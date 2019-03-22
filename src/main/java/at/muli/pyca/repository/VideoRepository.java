package at.muli.pyca.repository;

import at.muli.pyca.po.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

    Video findByVideoId(String videoId);
}
