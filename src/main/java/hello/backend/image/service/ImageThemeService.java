package hello.backend.image.service;

import hello.backend.image.dto.ThemeResponse;
import hello.backend.image.repository.ImageThemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageThemeService {

    private final ImageThemeRepository imageThemeRepository;

    public List<ThemeResponse> getThemes() {
        return imageThemeRepository.findAll().stream()
                .map(it -> new ThemeResponse(it.getId(), it.getTheme()))
                .collect(Collectors.toList());
    }
}
