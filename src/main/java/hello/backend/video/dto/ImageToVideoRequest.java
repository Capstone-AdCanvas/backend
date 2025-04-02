package hello.backend.video.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ImageToVideoRequest {
    private String imageUrl;
    private String prompt;
    private String aspect_ratio;
    private Integer duration;
}
