package hello.backend.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogoResponse {
    private Long id;
    private Long userId;
    private String name;
    private String logoImage;
}
