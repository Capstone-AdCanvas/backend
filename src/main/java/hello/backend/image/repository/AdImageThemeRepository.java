package hello.backend.image.repository;

import hello.backend.image.domain.AdImageTheme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdImageThemeRepository extends JpaRepository<AdImageTheme, Long> {
}
