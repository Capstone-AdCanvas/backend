package hello.backend.tts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TTSResponse {
    private String ttsPath;
    private String text;
}
