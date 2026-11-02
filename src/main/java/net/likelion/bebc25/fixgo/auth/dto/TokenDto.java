package net.likelion.bebc25.fixgo.auth.dto;

public class TokenDto {
    public record RefreshRequest(
            String refreshToken
    ) {}

    public record Response(
            String accessToken,
            String refreshToken,
            String tokenType,
            Long expiresIn
    ) {
        public static Response of(String accessToken, String refreshToken) {
            return new Response(accessToken, refreshToken, "Bearer", 3600L);
        }
    }
}
