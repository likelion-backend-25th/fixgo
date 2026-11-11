package net.likelion.bebc25.fixgo.AI.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class OpenAIDto {
    public record TestRequest(
            @Schema(description = "사용자가 입력한 텍스트", example = "안녕")
            String text
    ) {}
}
