package at.muli.pyca.repository;

import at.muli.pyca.po.CommentPO;
import at.muli.pyca.po.VideoPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

public interface CommentRepository extends JpaRepository<CommentPO, Long> {

    Stream<CommentPO> findAllByVideoOrderByDateAddDesc(VideoPO videoPO);
}
