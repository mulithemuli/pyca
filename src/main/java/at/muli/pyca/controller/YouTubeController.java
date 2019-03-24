package at.muli.pyca.controller;

import at.muli.pyca.bo.Author;
import at.muli.pyca.bo.Comment;
import at.muli.pyca.bo.YouTubeInfo;
import at.muli.pyca.po.AuthorPO;
import at.muli.pyca.po.CommentPO;
import at.muli.pyca.po.SeenCommentsPO;
import at.muli.pyca.po.VideoPO;
import at.muli.pyca.repository.AuthorRepository;
import at.muli.pyca.repository.CommentRepository;
import at.muli.pyca.repository.SeenCommentsRepository;
import at.muli.pyca.repository.VideoRepository;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api")
public class YouTubeController {

    private VideoRepository videoRepository;

    private CommentRepository commentRepository;

    private AuthorRepository authorRepository;

    private SeenCommentsRepository seenCommentsRepository;

    public YouTubeController(VideoRepository videoRepository,
                             CommentRepository commentRepository,
                             AuthorRepository authorRepository,
                             SeenCommentsRepository seenCommentsRepository) {
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.authorRepository = authorRepository;
        this.seenCommentsRepository = seenCommentsRepository;
    }

    @RequestMapping(path = "/video/{videoId}", method = RequestMethod.GET)
    public ResponseEntity<YouTubeInfo> loadVideoInfoById(@PathVariable("videoId") String videoId) {
        YouTubeInfo youTubeInfo = YouTubeInfo.fromVideo(videoRepository.findByVideoId(videoId));
        if (youTubeInfo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(youTubeInfo);
    }

    @RequestMapping(path = "/video", method = RequestMethod.GET)
    public ResponseEntity<YouTubeInfo> loadVideoInfo(@RequestParam("url") String url) {
        try {
            URL originalUrl = new URL(url);
            Document document = Jsoup.connect(url).get();
            Elements elements = document.select("title");
            String title = elements.first().text().replace(" - YouTube", "");
            if ("YouTube".equals(title.trim()) || "".equals(title.trim())) {
                return ResponseEntity.notFound().build();
            }
            String videoId = null;
            for (String paramValue : originalUrl.getQuery().split("&")) {
                String[] param = paramValue.split("=");
                if ("v".equals(param[0])) {
                    videoId = param[1];
                    break;
                }
            }
            String embed = String.format("%s://%s/embed/%s", originalUrl.getProtocol(), originalUrl.getHost(), videoId);
            return ResponseEntity.ok(YouTubeInfo.builder().embed(embed).title(title).url(url).videoId(videoId).build());
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(path = "/video", method = RequestMethod.POST)
    public ResponseEntity<YouTubeInfo> saveVideo(@RequestBody YouTubeInfo youTubeInfo) {
        VideoPO video = videoRepository.findByVideoId(youTubeInfo.getVideoId());
        if (video != null) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        videoRepository.save(youTubeInfo.toVideo());
        return ResponseEntity.ok(youTubeInfo);
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/videos", method = RequestMethod.GET)
    public List<YouTubeInfo> loadVideos() {
        return videoRepository.findAllByOrderByDateAddDesc().map(YouTubeInfo::fromVideo).collect(Collectors.toList());
    }

    @RequestMapping(path = "/comment", method = RequestMethod.POST)
    public Comment saveComment(@RequestBody Comment comment) {
        VideoPO video = videoRepository.findByVideoId(comment.getVideoId());
        CommentPO commentPo = CommentPO.builder()
                .text(comment.getText())
                .author(comment.getAuthor())
                .dateAdd(Instant.now())
                .video(video)
                .build();
        commentRepository.save(commentPo);
        return Comment.fromPo(commentPo);
    }

    @Transactional(readOnly = true)
    @RequestMapping(path = "/comments/{videoId}", method = RequestMethod.GET)
    public List<Comment> loadComments(@PathVariable("videoId") String videoId) {
        return commentRepository.findAllByVideoOrderByDateAddDesc(videoRepository.findByVideoId(videoId))
                .map(Comment::fromPo)
                .collect(Collectors.toList());
    }

    @RequestMapping(path = "/author", method = RequestMethod.PUT)
    public ResponseEntity<Author> addAuthor(@RequestBody Author author) {
        if (authorRepository.findByName(author.getName()) != null) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }
        authorRepository.save(AuthorPO.builder().name(author.getName()).build());
        return ResponseEntity.ok(author);
    }

    @RequestMapping(path = "/author", method = RequestMethod.POST)
    public Author updateLastSeen(@RequestBody Author author) {
        AuthorPO authorPO = authorRepository.findByName(author.getName());
        author.getSeenComments().entrySet().stream().forEach(e -> {
            VideoPO videoPO = videoRepository.findByVideoId(e.getKey());
            SeenCommentsPO seenCommentsPO = seenCommentsRepository.findByAuthorAndVideo(authorPO, videoPO);
            if (seenCommentsPO == null) {
                seenCommentsPO = SeenCommentsPO.builder().video(videoPO).author(authorPO).build();
            }
            seenCommentsPO.setComments(e.getValue());
            seenCommentsRepository.save(seenCommentsPO);
        });
        return author;
    }

    @RequestMapping(path = "/author/{author}", method = RequestMethod.GET)
    public ResponseEntity<Author> loadAuthor(@PathVariable("author") String author) {
        AuthorPO authorPO = authorRepository.findByName(author);
        if (authorPO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Author.builder()
                .name(author)
                .seenComments(authorPO.getSeenComments()
                        .stream()
                        .collect(Collectors.toMap(s -> s.getVideo().getVideoId(), SeenCommentsPO::getComments)))
                .build());

    }
}
