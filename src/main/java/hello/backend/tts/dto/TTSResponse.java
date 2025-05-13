package hello.backend.tts.dto;

import hello.backend.tts.dto.enums.TTSModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TTSResponse {
    private String name;
    private String code;
    private String gender;

    public static TTSResponse from(TTSModel model) {
        return new TTSResponse(
                model.getName(),
                model.getCode(),
                model.getGender()
        );
    }
}
