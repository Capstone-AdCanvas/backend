package hello.backend.image.controller;

import hello.backend.image.domain.AdImage;
import hello.backend.image.dto.ImageResponse;
import hello.backend.image.service.AdImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final AdImageService adImageService;

    @Operation(summary = "이미지 업로드", description = "상품의 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "500", description = "잘못된 요청: 업로드 실패")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("userId") Long userId,
            @RequestParam("image") MultipartFile image) throws IOException {

        AdImage savedImage = adImageService.uploadImage(userId, image);

        ImageResponse response = new ImageResponse(
                savedImage.getId(),
                savedImage.getUser().getId(),
                savedImage.getOriginalImage(),
                savedImage.getCreatedAt(),
                savedImage.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }
}
