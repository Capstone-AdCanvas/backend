package hello.backend.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombineImageRequest {
    private String baseImage;
    private List<OverlayItemRequest> overlays;
}

