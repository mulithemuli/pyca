package at.muli.pyca.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "comment")
public class CommentPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String author;

    private String text;

    private Instant dateAdd;

    @ManyToOne
    @JoinColumn(name = "id_video", referencedColumnName = "id", nullable = false)
    private VideoPO video;
}
