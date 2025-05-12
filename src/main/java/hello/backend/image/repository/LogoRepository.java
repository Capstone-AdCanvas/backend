package hello.backend.image.repository;

import hello.backend.image.domain.Logo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogoRepository extends JpaRepository<Logo, Long> {
    List<Logo> findAllByUserId(Long user_id);
}
