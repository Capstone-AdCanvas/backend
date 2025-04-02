package hello.backend.video.contoller;

import com.google.gson.JsonObject;
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

    @Operation(summary = "text to video 생성", description = "텍스트를 기반으로 영상을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영상 생성 성공")
    })
    @PostMapping("/text")
    public ResponseEntity<TextToVideoResponse> createTextToVideo(@RequestBody TextToVideoRequest request) {
        JsonObject createTextToVideo = videoService.createTextToVideo(request);
        TextToVideoResponse response = TextToVideoResponse.from(createTextToVideo,
                request.getAspect_ratio(),
                request.getDuration());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "image to video 생성", description = "이미지를 기반으로 영상을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "영상 생성 성공")
    })
    @PostMapping("/image")
    public ResponseEntity<ImageToVideoResponse> createImageToVideo(@RequestBody ImageToVideoRequest request) {
        JsonObject createImageToVideo = videoService.createImageToVideo(request);
        ImageToVideoResponse response = ImageToVideoResponse.from(createImageToVideo,
                request.getDuration()
        , request.getAspect_ratio());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
}
