package hello.backend.image.config;

import hello.backend.image.domain.ImageTheme;
import hello.backend.image.repository.ImageThemeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ImageThemeInitializer implements CommandLineRunner {
    private final ImageThemeRepository imageThemeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initialize();
    }

    @Transactional
    public void initialize() {
        if (imageThemeRepository.count() > 0) {
            return;
        }

        List<String> themes = List.of(
                "auto",
                "studio",
                "office",
                "city",
                "spring",
                "summer",
                "fall",
                "winter",
                "simple",
                "with_plant",
                "table",
                "minimalism"
        );

        for (String theme : themes) {
            ImageTheme imageTheme = new ImageTheme();
            imageTheme.setTheme(theme);
            imageThemeRepository.save(imageTheme);
        }
    }
}
