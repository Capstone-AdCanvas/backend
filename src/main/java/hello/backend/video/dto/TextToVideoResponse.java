package hello.backend.video.dto;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TextToVideoResponse {
    private String videoUrl;
    private String aspect_ratio;
    private String duration;
    private LocalDateTime createdAt;

    public static TextToVideoResponse from(JsonObject result, String aspectRatio, String duration) {
        if (result == null || !result.has("video")) {
            throw new IllegalStateException("Fal 응답에 video 필드 없음: " + result);
        }

        JsonObject videoObj = result.getAsJsonObject("video");

        if (!videoObj.has("url")) {
            throw new IllegalStateException("Fal 응답의 video 객체에 url 없음: " + result);
        }

        return new TextToVideoResponse(
                videoObj.get("url").getAsString(),
                aspectRatio,
                duration,
                LocalDateTime.now()
        );
    }
}
