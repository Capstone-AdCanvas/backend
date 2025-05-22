package hello.backend.tts.dto;

import hello.backend.tts.dto.enums.TTSModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TTSModelResponse {
    private String name;
    private String code;
    private String gender;

    public static TTSModelResponse from(TTSModel model) {
        return new TTSModelResponse(
                model.getName(),
                model.getCode(),
                model.getGender()
        );
    }
}
