package hello.backend.image.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hello.backend.image.dto.*;
import hello.backend.image.service.ImageBgService;
import hello.backend.image.service.ImageSizeService;
import hello.backend.image.service.ImageThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/image/bg")
@RequiredArgsConstructor
public class ImageBgController {

    private final ImageBgService imageBgService;
    private final ImageThemeService imageThemeService;
    private final ImageSizeService imageSizeService;

    @Operation(summary = "이미지 배경 제거", description = "상품의 배경을 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (이미지 응답 없음)"),
            @ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음")
    })
    @PostMapping("/{imageId}/remove")
    public ResponseEntity<BgRemoveResponse> removeBg(@PathVariable Long imageId) throws JsonProcessingException {
        BgRemoveResponse response = imageBgService.removeBg(imageId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "이미지 테마 조회", description = "이미지의 테마를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/themes")
    public ResponseEntity<List<ThemeResponse>> getThemes() {
        List<ThemeResponse> themes = imageThemeService.getThemes();
        return new ResponseEntity<>(themes, HttpStatus.OK);
    }

    @Operation(summary = "이미지 사이즈 조회", description = "이미지의 사이즈를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("/sizes")
    public ResponseEntity<List<SizeResponse>> getSizes() {
        List<SizeResponse> sizes = imageSizeService.getImageSizes();
        return new ResponseEntity<>(sizes, HttpStatus.OK);
    }

    @Operation(summary = "이미지 배경 생성", description = "상품의 배경을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (응답 오류)"),
            @ApiResponse(responseCode = "404", description = "이미지/테마/비율을 찾을 수 없음")
    })
    @PostMapping("/{imageId}/generate")
    public ResponseEntity<List<BgGenerateResponse>> generateBg(
            @PathVariable Long imageId,
            @RequestBody BgGenerateRequest request
    ) throws JsonProcessingException {
        List<BgGenerateResponse> response = imageBgService.generateBg(imageId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "이미지 배경 커스텀 생성", description = "상품의 배경을 커스텀하여 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (응답 오류)"),
            @ApiResponse(responseCode = "404", description = "비율을 찾을 수 없음")
    })
    @PostMapping("/{imageId}/custom-generate")
    public ResponseEntity<List<BgGenerateResponse>> generateCustomBg(
            @PathVariable Long imageId,
            @RequestBody BgCustomGenerateRequest request
    ) throws JsonProcessingException {
        List<BgGenerateResponse> response = imageBgService.generateCustomBg(imageId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "최종 이미지 선택", description = "생성한 이미지 중 사용할 이미지의 최종 선택을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공"),
            @ApiResponse(responseCode = "400", description = "파일 이동 오류"),
    })
    @PostMapping("/{imageId}/select-finalImage")
    public ResponseEntity<FinalImageResponse> selectFinalImage(
            @PathVariable Long imageId,
            @RequestBody FinalImageRequest request
    ){
        FinalImageResponse response = imageBgService.selectFinalImage(imageId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
