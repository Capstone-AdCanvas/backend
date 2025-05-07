package hello.backend.tts.service;

import hello.backend.error.ErrorCode;
import hello.backend.error.exception.BusinessException;
import hello.backend.tts.dto.TTSRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class TTSService {
    private final WebClient clovaTtsWebClient;

    public TTSService(@Qualifier("clovaTtsWebClient") WebClient clovaTtsWebClient) {
        this.clovaTtsWebClient = clovaTtsWebClient;
    }

    // tts 변환
    public byte[] getTtsAudio(TTSRequest ttsRequest) {
        return clovaTtsWebClient.post()
                .uri("/tts")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("speaker", ttsRequest.getSpeaker())
                        .with("text", ttsRequest.getText()))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .map(TTSService::extractClovaErrorCode)
                                .map(BusinessException::new)
                                .flatMap(Mono::error)
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new BusinessException(ErrorCode.CLOVA_TTS_INTERNAL_ERROR))
                )
                .bodyToMono(byte[].class)
                .block();
    }

    private static ErrorCode extractClovaErrorCode(String body) {
        if (body.contains("VS01")) return ErrorCode.CLOVA_TTS_SPEAKER_REQUIRED;
        if (body.contains("VS02")) return ErrorCode.CLOVA_TTS_UNSUPPORTED_SPEAKER;
        if (body.contains("VS05")) return ErrorCode.CLOVA_TTS_TEXT_REQUIRED;
        if (body.contains("VS06")) return ErrorCode.CLOVA_TTS_TEXT_TOO_LONG;
        return ErrorCode.CLOVA_TTS_INTERNAL_ERROR;
    }
}
