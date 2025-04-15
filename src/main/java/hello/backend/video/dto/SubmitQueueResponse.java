package hello.backend.video.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubmitQueueResponse {
    String requestId;
    String message;
}
