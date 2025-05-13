package hello.backend.tts.controller;

import hello.backend.tts.dto.TTSRequest;
import hello.backend.tts.dto.TTSResponse;
import hello.backend.tts.service.TTSService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tts")
@RequiredArgsConstructor
public class TTSController {
    private final TTSService ttsService;

    @Operation(summary = "tts 변환", description = "대본을 바탕으로 보이스를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("")
    public ResponseEntity<byte[]> ConvertTts(@RequestBody TTSRequest ttsRequest) {
        byte[] audioData = ttsService.getTtsAudio(ttsRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("inline")
                .filename("speech.mp3")
                .build());

        return new ResponseEntity<>(audioData, headers, HttpStatus.CREATED);
    }

    @Operation(summary = "tts 모델 조회", description = "tts 모델을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping("")
    public ResponseEntity<List<TTSResponse>> GetTtsModel() {
        return new ResponseEntity<>(ttsService.getTTSModel(), HttpStatus.OK);
    }
}
