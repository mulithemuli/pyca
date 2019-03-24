package at.muli.pyca.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "seen_comments")
public class SeenCommentsPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private Long comments;

    @ManyToOne
    @JoinColumn(name = "id_author", referencedColumnName = "id", nullable = false)
    private AuthorPO author;

    @ManyToOne
    @JoinColumn(name = "id_video", referencedColumnName = "id", nullable = false)
    private VideoPO video;

}
