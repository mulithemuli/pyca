package at.muli.pyca.repository;

import at.muli.pyca.po.AuthorPO;
import at.muli.pyca.po.SeenCommentsPO;
import at.muli.pyca.po.VideoPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface SeenCommentsRepository extends JpaRepository<SeenCommentsPO, Long> {

    Collection<SeenCommentsPO> findAllByAuthor(AuthorPO author);

    SeenCommentsPO findByAuthorAndVideo(AuthorPO author, VideoPO video);
}
