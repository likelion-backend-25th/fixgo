package net.likelion.bebc25.fixgo.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public class LoginDto {
    public record LoginRequest(
            @Email @NotNull String email,
            @NotNull String password
    ) {}
}
