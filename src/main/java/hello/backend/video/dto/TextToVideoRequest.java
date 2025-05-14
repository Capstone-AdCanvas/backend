package hello.backend.video.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TextToVideoRequest {
    private Integer second;
    private String prompt;
    private String aspect_ratio;
    private String duration;
}
