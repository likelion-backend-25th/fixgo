package net.likelion.bebc25.fixgo.auth.dto;

public class LoginDto {
    public record LoginRequest(
            String email,
            String password
    ) {}
}
