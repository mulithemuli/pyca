package at.muli.pyca.repository;

import at.muli.pyca.po.VideoPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

public interface VideoRepository extends JpaRepository<VideoPO, Long> {

    VideoPO findByVideoId(String videoId);

    Stream<VideoPO> findAllByOrderByDateAddDesc();
}
