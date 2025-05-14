package hello.backend.audio.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class MergeRequest {
    private List<String> videoUrls;
    private String tema;
}
