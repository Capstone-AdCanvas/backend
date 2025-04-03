package hello.backend.image.dto;

import hello.backend.image.domain.enums.ImageTheme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {
    private String theme;

    public static ThemeResponse getTheme(ImageTheme imageTheme) {
        return new ThemeResponse(imageTheme.name());
    }
}
