package at.muli.pyca.bo;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class YouTubeInfo {

    private String title;

    private String url;

    private String embed;

    private String videoId;

    private String author;
}
