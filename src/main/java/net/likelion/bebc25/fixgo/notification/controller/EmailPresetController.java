package net.likelion.bebc25.fixgo.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailPresetDto;
import net.likelion.bebc25.fixgo.notification.service.EmailPresetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email-presets")
@RequiredArgsConstructor
public class EmailPresetController {

    private final EmailPresetService emailPresetService;

    // 이메일 프리셋 등록
    @PostMapping
    public ResponseEntity<EmailPresetDto.Response> createPreset(
            @Valid @RequestBody EmailPresetDto.CreateRequest request
    ) {
        EmailPresetDto.Response response = emailPresetService.createPreset(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 이메일 프리셋 목록 조회
    @GetMapping
    public ResponseEntity<List<EmailPresetDto.Response>> getPresetList() {
        return ResponseEntity.ok(emailPresetService.getPresetList());
    }

    // 이메일 프리셋 수정
    @PutMapping("/{id}")
    public ResponseEntity<EmailPresetDto.Response> updatePreset(
            @PathVariable Long id,
            @Valid @RequestBody EmailPresetDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(emailPresetService.updatePreset(id, request));
    }

    // 이메일 프리셋 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreset(@PathVariable Long id) {
        emailPresetService.deletePreset(id);

        return ResponseEntity.ok().build();
    }


}
