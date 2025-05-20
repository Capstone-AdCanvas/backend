package hello.backend.image.dto;

import lombok.Data;

@Data
public class OverlayItemRequest {
    private String type; // "logo" or "text"

    // 공통 속성
    private int x;
    private int y;

    // logo용
    private String imageUrl;
    private double scale;

    // text용
    private String text;
    private String font;
    private int size;
    private String color;
}
