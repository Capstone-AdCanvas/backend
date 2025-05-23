package hello.backend.tts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSPreviewRequest {
    private String speaker;
    private String text;
    private int emotion;
    private int emotionStrength;
}
