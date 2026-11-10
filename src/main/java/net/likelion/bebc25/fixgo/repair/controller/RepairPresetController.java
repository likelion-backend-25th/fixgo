package net.likelion.bebc25.fixgo.repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.RepairPresetPartResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetRequest;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetUpdate;
import net.likelion.bebc25.fixgo.repair.service.RepairPresetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-presets")
public class RepairPresetController {

    private final RepairPresetService repairPresetService;

    // 프리셋 등록
    @PostMapping
    public ResponseEntity<RepairPresetResponse> createPreset(
            @Valid @RequestBody RepairPresetRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairPresetService.createPreset(request));
    }

    // 전체 프리셋 조회
    @GetMapping
    public ResponseEntity<List<RepairPresetResponse>> getAllPresets() {
        return ResponseEntity.ok(repairPresetService.getAllPresets());
    }

    // 단일 프리셋 조회
    @GetMapping("/{id}")
    public ResponseEntity<RepairPresetResponse> getPreset(@PathVariable Long id) {
        return ResponseEntity.ok(repairPresetService.getPreset(id));
    }

    // 프리셋 수정
    @PutMapping("/{id}")
    public ResponseEntity<RepairPresetResponse> updatePreset(
            @PathVariable Long id,
            @Valid @RequestBody RepairPresetUpdate request
    ) {
        return ResponseEntity.ok(repairPresetService.updatePreset(id, request));
    }

    // 프리셋 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreset(@PathVariable Long id) {
        repairPresetService.deletePreset(id);
        return ResponseEntity.noContent().build();
    }

    // 프리셋 이름 검색
    @GetMapping("/search")
    public ResponseEntity<List<RepairPresetResponse>> searchPresets(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(repairPresetService.searchPresets(name));
    }

    @GetMapping("/{id}/parts")
    public ResponseEntity<List<RepairPresetPartResponse>> getPresetParts(@PathVariable Long id) {
        return ResponseEntity.ok(repairPresetService.getPresetParts(id));
    }
}