package hello.backend.image.controller;
import hello.backend.image.domain.AdImage;
import hello.backend.image.dto.BgRemoveResponse;
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
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 업로드 오류"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "415", description = "지원되지 않는 미디어 타입")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> uploadImage(
            @RequestParam("userId") Long userId,
            @RequestParam("image") MultipartFile image) throws IOException {
        ImageResponse response = adImageService.uploadImage(userId, image);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "모든 이미지 조회", description = "모든 이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
    })
    @GetMapping("")
    public ResponseEntity<List<ImageResponse>> getAllImages() {
        List<ImageResponse> images = adImageService.getAllImages();
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @Operation(summary = "특정 회원 이미지 조회", description = "특정 회원의 이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ImageResponse>> getUserImages(
            @PathVariable Long userId
    ) {
        List<ImageResponse> images = adImageService.getUserImages(userId);
        return new ResponseEntity<>(images, HttpStatus.OK);
    }

    @Operation(summary = "특정 이미지 조회", description = "특정 이미지를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @GetMapping("/{imageId}")
    public ResponseEntity<ImageResponse> getImage(@PathVariable Long imageId) {
        ImageResponse image = adImageService.getImage(imageId);
        return new ResponseEntity<>(image, HttpStatus.OK);
    }

    @Operation(summary = "이미지 배경 제거", description = "상품의 배경을 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미지 응답 없음)"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @PostMapping("/{imageId}/remove-bg")
    public ResponseEntity<BgRemoveResponse> removeBg(@PathVariable Long imageId) {
            BgRemoveResponse response = adImageService.removeBg(imageId);
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
