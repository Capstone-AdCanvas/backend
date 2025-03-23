package hello.backend.image.controller;

import hello.backend.image.domain.AdImage;
import hello.backend.image.dto.ImageResponse;
import hello.backend.image.service.AdImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/image")
@RequiredArgsConstructor
public class ImageController {

    private final AdImageService adImageService;

    @Operation(summary = "이미지 업로드", description = "상품의 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "파일 문제"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("userId") Long userId,
            @RequestParam("image") MultipartFile image) throws IOException {
        ImageResponse response = adImageService.uploadImage(userId, image);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
