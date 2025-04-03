package hello.backend.image.service;

import hello.backend.image.domain.enums.ImageTheme;
import hello.backend.image.dto.ThemeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageThemeService {
    public List<ThemeResponse> getThemes() {
        return List.of(ImageTheme.values()).stream()
                .map(ThemeResponse::getTheme)
                .collect(Collectors.toList());
    }
}
