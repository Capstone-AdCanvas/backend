package hello.backend.image.service;

import hello.backend.image.domain.enums.ImageSize;
import hello.backend.image.dto.SizeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageSizeService {
    public List<SizeResponse> getImageSizes() {
        return List.of(ImageSize.values()).stream()
                .map(size -> new SizeResponse(size.getWidth(), size.getHeight(), size.getRatio()))
                .toList();
    }
}
