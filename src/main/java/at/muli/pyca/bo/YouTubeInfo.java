package at.muli.pyca.bo;

import at.muli.pyca.po.VideoPO;
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

    public VideoPO toVideo() {
        return VideoPO.builder()
                .author(author)
                .embed(embed)
                .title(title)
                .url(url)
                .videoId(videoId)
                .dateAdd(Instant.now())
                .build();
    }

    public static YouTubeInfo fromVideo(VideoPO video) {
        if (video == null) {
            return null;
        }
        return builder()
                .title(video.getTitle())
                .url(video.getUrl())
                .embed(video.getEmbed())
                .videoId(video.getVideoId())
                .author(video.getAuthor())
                .build();
    }
}
