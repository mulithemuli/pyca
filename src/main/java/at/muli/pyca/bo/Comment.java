package at.muli.pyca.bo;

import at.muli.pyca.po.CommentPO;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class Comment {

    private String author;

    private String text;

    private Instant dateAdd;

    private String videoId;

    public static Comment fromPo(CommentPO commentPO) {
        return Comment.builder()
                .author(commentPO.getAuthor())
                .text(commentPO.getText())
                .dateAdd(commentPO.getDateAdd())
                .build();
    }
}
