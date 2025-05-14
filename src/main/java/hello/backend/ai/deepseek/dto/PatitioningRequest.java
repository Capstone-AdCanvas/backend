package hello.backend.ai.deepseek.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PatitioningRequest {
    private String prompt;
    private Integer second;
}
