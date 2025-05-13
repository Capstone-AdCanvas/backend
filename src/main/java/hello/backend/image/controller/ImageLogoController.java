package hello.backend.image.controller;

import hello.backend.image.dto.ImageResponse;
import hello.backend.image.dto.LogoResponse;
import hello.backend.image.service.ImageLogoService;
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
@RequiredArgsConstructor
@RequestMapping("/api/v1/image/logo")
public class ImageLogoController {
    private final ImageLogoService imageLogoService;

    @Operation(summary = "로고 업로드", description = "상품의 이미지를 업로드합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 업로드 오류"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "415", description = "지원되지 않는 미디어 타입")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LogoResponse> uploadImage(
            @RequestParam("userId") Long userId,
            @RequestParam("image") MultipartFile image) throws IOException {
        LogoResponse response = imageLogoService.uploadImage(userId, image);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "사용자 로고 조회", description = "사용자가 업로드한 로고를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 업로드 오류"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
    })
    @GetMapping("/")
    public ResponseEntity<List<LogoResponse>> getLogo(@RequestParam("userId") Long userId) {
        List<LogoResponse> response = imageLogoService.getLogoImage(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
