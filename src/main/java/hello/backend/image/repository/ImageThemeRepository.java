package hello.backend.image.repository;

import hello.backend.image.domain.ImageTheme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageThemeRepository extends JpaRepository<ImageTheme, Long> {
}
