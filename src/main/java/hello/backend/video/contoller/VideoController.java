package hello.backend.video.contoller;

import hello.backend.ai.deepseek.service.DeepSeekService;
import hello.backend.video.dto.*;
import hello.backend.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;
    private final DeepSeekService deepSeekService;

    @Operation(summary = "text to video 생성(Kling-pro-v1.6 모델)", description = "텍스트를 기반으로 영상을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영상 생성 성공")
    })
    @PostMapping("/texts/Kling-pro")
    public ResponseEntity<TextToVideoResponse> createTextToVideoKling(@RequestBody TextToVideoRequest request) {
        TextToVideoResponse createTextToVideo = videoService.createTextToVideo(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createTextToVideo);
    }

    @Operation(summary = "image to video 생성(Kling-pro-v1.6 모델)", description = "이미지를 기반으로 영상을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영상 생성 성공")
    })
    @PostMapping("/images/Kling-pro")
    public ResponseEntity<ImageToVideoResponse> createImageToVideoKling(@RequestBody ImageToVideoRequest request) {
        ImageToVideoResponse createImageToVideo = videoService.createImageToVideo(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(createImageToVideo);
    }

    @Operation(summary = "비디오 저장", description = "생성된 비디오를 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비디오 저장 성공"),
            @ApiResponse(responseCode = "404", description = "잘못된 요청: 회원을 찾을 수 없음")
    })
    @PostMapping("/{userId}")
    public ResponseEntity<SaveResponse> saveVideo(
            @Parameter(description = "저장할 회원의 ID", required = true)
            @PathVariable Long userId,
            @RequestBody SaveRequest request) {

        SaveResponse response = videoService.saveVideo(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "DeepSeek prompt 테스트(text)", description = "prompt를 DeepSeek에 보내 변환된 결과를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변환 성공")
    })
    @PostMapping("/transforms/Text")
    public ResponseEntity<String> textTransFormScript(@RequestBody String prompt) {
        String result = deepSeekService.textTransFormScript(prompt);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "DeepSeek prompt 테스트(image)", description = "prompt를 DeepSeek에 보내 변환된 결과를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "변환 성공")
    })
    @PostMapping("/transforms/Image")
    public ResponseEntity<String> imagetransformPrompt(@RequestBody String prompt) {
        String result = deepSeekService.imageTransFormScript(prompt);
        return ResponseEntity.ok(result);
    }
}
