package at.muli.pyca.controller;

import at.muli.pyca.bo.YouTubeInfo;
import at.muli.pyca.po.Video;
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
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api")
public class YouTubeController {

    private VideoRepository videoRepository;

    public YouTubeController(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @RequestMapping(path = "/video/{videoId}", method = RequestMethod.GET)
    public ResponseEntity<YouTubeInfo> loadVideoInfoById(@PathVariable("videoId") String videoId) {
        log.info(videoId);
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
        Video video = videoRepository.findByVideoId(youTubeInfo.getVideoId());
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
}
