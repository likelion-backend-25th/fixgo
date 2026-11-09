package net.likelion.bebc25.fixgo.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.dto.LoginDto;
import net.likelion.bebc25.fixgo.auth.dto.TokenDto;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.auth.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<TokenDto.Response> login(
            @RequestBody @Valid LoginDto.LoginRequest request
    ) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenDto.Response> refresh(
            @RequestBody @Valid TokenDto.RefreshRequest request
    ) {
        return ResponseEntity.ok(loginService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        loginService.logout(userDetails);
        return ResponseEntity.ok().build();
    }
}
