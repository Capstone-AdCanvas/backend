package hello.backend.image.config;

import hello.backend.image.domain.AdImageTheme;
import hello.backend.image.repository.AdImageThemeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdImageThemeInitializer implements CommandLineRunner {
    private final AdImageThemeRepository adImageThemeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initialize();
    }

    @Transactional
    public void initialize() {
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
            AdImageTheme adImageTheme = new AdImageTheme();
            adImageTheme.setTheme(theme);
            adImageThemeRepository.save(adImageTheme);
        }
    }
}
