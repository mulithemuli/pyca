package at.muli.pyca.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "video")
public class VideoPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private String url;

    private String embed;

    private String videoId;

    private String title;

    private String author;

    private Instant dateAdd;

    @OneToMany(mappedBy = "video")
    private List<CommentPO> comments;
}
