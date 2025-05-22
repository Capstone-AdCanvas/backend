package hello.backend.tts.controller;

import hello.backend.tts.dto.TTSPreviewRequest;
import hello.backend.tts.dto.TTSRequest;
import hello.backend.tts.dto.TTSModelResponse;
import hello.backend.tts.dto.TTSResponse;
import hello.backend.tts.service.TTSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tts")
@RequiredArgsConstructor
public class TTSController {
    private final TTSService ttsService;

    @Value("${file.tts-url}")
    private String ttsUrl;

    @Operation(summary = "TTS 변환", description = "대본을 바탕으로 보이스를 생성하고 URL 리스트를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    public Mono<ResponseEntity<List<TTSResponse>>> convertTts(@RequestBody TTSRequest ttsRequest) {
        return ttsService.getTtsAudioListAsync(ttsRequest)
                .map(fileNames -> fileNames.stream()
                        .map(name -> new TTSResponse(ttsUrl + name))
                        .collect(Collectors.toList()))
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "TTS 미리듣기", description = "생성된 대본을 바탕으로 TTS 모델의 음성을 들어봅니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/preview")
    public Mono<ResponseEntity<TTSResponse>> convertTtsListen(@RequestBody TTSPreviewRequest request) {
        return ttsService.generatePreviewAudio(request)
                .map(ttsResponse -> ResponseEntity.status(201).body(ttsResponse));
    }

    @Operation(summary = "tts 모델 조회", description = "tts 모델을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("")
    public ResponseEntity<List<TTSModelResponse>> getTtsModel() {
        return new ResponseEntity<>(ttsService.getTTSModel(), HttpStatus.OK);
    }
}
