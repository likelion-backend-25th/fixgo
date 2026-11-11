package net.likelion.bebc25.fixgo.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth API", description = "로그인/로그아웃 및 토큰 재발급 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 Access/Refresh 토큰을 발급한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<TokenDto.Response> login(
            @RequestBody @Valid LoginDto.LoginRequest request
    ) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    public ResponseEntity<TokenDto.Response> refresh(
            @RequestBody @Valid TokenDto.RefreshRequest request
    ) {
        return ResponseEntity.ok(loginService.refresh(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인 사용자의 Refresh Token을 무효화한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        loginService.logout(userDetails);
        return ResponseEntity.ok().build();
    }
}
