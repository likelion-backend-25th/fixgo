package net.likelion.bebc25.fixgo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TokenDto {
    public record RefreshRequest(
            @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
            String refreshToken
    ) {}

    public record Response(
            @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
            String accessToken,
            @Schema(description = "Refresh Token", example = "eyJhbGciOiJIUzI1NiJ9...")
            String refreshToken,
            @Schema(description = "토큰 타입", example = "Bearer")
            String tokenType,
            @Schema(description = "Access Token 만료 시간(초)", example = "3600")
            Long expiresIn
    ) {
        public static Response of(String accessToken, String refreshToken) {
            return new Response(accessToken, refreshToken, "Bearer", 3600L);
        }
    }
}
