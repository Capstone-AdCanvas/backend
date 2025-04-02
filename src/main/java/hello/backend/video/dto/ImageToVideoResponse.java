package hello.backend.video.dto;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ImageToVideoResponse {
    private String videoUrl;
    private Integer duration;
    private String aspect_ratio;
    private LocalDateTime createdAt;

    public static ImageToVideoResponse from(JsonObject result, Integer duration, String aspect_ratio) {
        if (result == null || !result.has("video")) {
            throw new IllegalStateException("Fal 응답에 video 필드 없음: " + result);
        }

        JsonObject videoObj = result.getAsJsonObject("video");

        if (!videoObj.has("url")) {
            throw new IllegalStateException("Fal 응답의 video 객체에 url 없음: " + result);
        }

        return new ImageToVideoResponse(
                videoObj.get("url").getAsString(),
                duration,
                aspect_ratio,
                LocalDateTime.now()
        );
    }
}
