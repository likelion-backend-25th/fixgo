package net.likelion.bebc25.fixgo.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class LoginDto {
    public record LoginRequest(
            @Schema(description = "로그인 이메일", example = "admin@fixgo.com")
            @Email @NotNull String email,
            @Schema(description = "로그인 비밀번호", example = "password123!")
            @NotNull String password
    ) {}
}
