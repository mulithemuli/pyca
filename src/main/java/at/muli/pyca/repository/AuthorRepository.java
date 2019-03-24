package at.muli.pyca.repository;

import at.muli.pyca.po.AuthorPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<AuthorPO, Long> {

    AuthorPO findByName(String name);
}
