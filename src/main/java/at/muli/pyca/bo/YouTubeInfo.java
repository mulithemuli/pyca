package at.muli.pyca.bo;

import at.muli.pyca.po.Video;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class YouTubeInfo {

    private String title;

    private String url;

    private String embed;

    private String videoId;

    private String author;

    public Video toVideo() {
        return Video.builder()
                .author(author)
                .embed(embed)
                .title(title)
                .url(url)
                .videoId(videoId)
                .dateAdd(Instant.now())
                .build();
    }

    public static YouTubeInfo fromVideo(Video video) {
        return builder()
                .title(video.getTitle())
                .url(video.getUrl())
                .embed(video.getEmbed())
                .videoId(video.getVideoId())
                .author(video.getAuthor())
                .build();
    }
}
