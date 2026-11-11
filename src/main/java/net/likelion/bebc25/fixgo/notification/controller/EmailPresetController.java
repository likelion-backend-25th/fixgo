package net.likelion.bebc25.fixgo.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailPresetDto;
import net.likelion.bebc25.fixgo.notification.service.EmailPresetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Email Preset API", description = "이메일 프리셋 등록/조회/수정/삭제 API")
@RestController
@RequestMapping("/api/v1/email-presets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
public class EmailPresetController {

    private final EmailPresetService emailPresetService;

    // 이메일 프리셋 등록
    @PostMapping
    @Operation(summary = "이메일 프리셋 등록", description = "이메일 프리셋을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프리셋 등록 성공"),
    })
    public ResponseEntity<EmailPresetDto.Response> createPreset(
            @Valid @RequestBody EmailPresetDto.CreateRequest request
    ) {
        EmailPresetDto.Response response = emailPresetService.createPreset(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 이메일 프리셋 목록 조회
    @GetMapping
    @Operation(summary = "이메일 프리셋 목록 조회", description = "이메일 프리셋 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 목록 조회 성공"),
    })
    public ResponseEntity<List<EmailPresetDto.Response>> getPresetList() {
        return ResponseEntity.ok(emailPresetService.getPresetList());
    }

    // 이메일 프리셋 수정
    @PutMapping("/{id}")
    @Operation(summary = "이메일 프리셋 수정", description = "이메일 프리셋을 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 수정 성공"),
    })
    public ResponseEntity<EmailPresetDto.Response> updatePreset(
            @PathVariable Long id,
            @Valid @RequestBody EmailPresetDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(emailPresetService.updatePreset(id, request));
    }

    // 이메일 프리셋 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "이메일 프리셋 삭제", description = "이메일 프리셋을 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 삭제 성공"),
    })
    public ResponseEntity<Void> deletePreset(@PathVariable Long id) {
        emailPresetService.deletePreset(id);

        return ResponseEntity.ok().build();
    }


}
