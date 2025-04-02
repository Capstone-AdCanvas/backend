package hello.backend.image.service;

import hello.backend.image.config.AdImageThemeInitializer;
import hello.backend.image.domain.AdImageTheme;
import hello.backend.image.dto.ThemeResponse;
import hello.backend.image.repository.AdImageThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdImageThemeService {
    private final AdImageThemeRepository adImageThemeRepository;

    public List<ThemeResponse> getThemes() {
        return adImageThemeRepository.findAll().stream()
                .map(it -> new ThemeResponse(it.getId(), it.getTheme()))
                .collect(Collectors.toList());
    }
}
