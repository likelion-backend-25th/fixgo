package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Repair Preset API", description = "수리 프리셋 등록·조회·수정·삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-presets")
public class RepairPresetController {

    private final RepairPresetService repairPresetService;

    // 프리셋 등록
    @PostMapping
    @Operation(summary = "프리셋 등록", description = "새로운 수리 프리셋을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "프리셋 등록 성공"),
    })
    public ResponseEntity<RepairPresetResponse> createPreset(
            @Valid @RequestBody RepairPresetRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairPresetService.createPreset(request));
    }

    // 전체 프리셋 조회
    @GetMapping
    @Operation(summary = "전체 프리셋 조회", description = "등록된 전체 수리 프리셋 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 목록 조회 성공"),
    })
    public ResponseEntity<List<RepairPresetResponse>> getAllPresets() {
        return ResponseEntity.ok(repairPresetService.getAllPresets());
    }

    // 단일 프리셋 조회
    @GetMapping("/{id}")
    @Operation(summary = "단일 프리셋 조회", description = "프리셋 ID로 수리 프리셋 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 상세 조회 성공"),
    })
    public ResponseEntity<RepairPresetResponse> getPreset(@PathVariable Long id) {
        return ResponseEntity.ok(repairPresetService.getPreset(id));
    }

    // 프리셋 수정
    @PutMapping("/{id}")
    @Operation(summary = "프리셋 수정", description = "수리 프리셋 정보를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 수정 성공"),
    })
    public ResponseEntity<RepairPresetResponse> updatePreset(
            @PathVariable Long id,
            @Valid @RequestBody RepairPresetUpdate request
    ) {
        return ResponseEntity.ok(repairPresetService.updatePreset(id, request));
    }

    // 프리셋 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "프리셋 삭제", description = "수리 프리셋을 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "프리셋 삭제 성공"),
    })
    public ResponseEntity<Void> deletePreset(@PathVariable Long id) {
        repairPresetService.deletePreset(id);
        return ResponseEntity.noContent().build();
    }

    // 프리셋 이름 검색
    @GetMapping("/search")
    @Operation(summary = "프리셋 이름 검색", description = "이름으로 수리 프리셋을 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 검색 성공"),
    })
    public ResponseEntity<List<RepairPresetResponse>> searchPresets(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(repairPresetService.searchPresets(name));
    }

    @GetMapping("/{id}/parts")
    @Operation(summary = "프리셋 부품 목록 조회", description = "수리 프리셋에 연결된 부품 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프리셋 부품 목록 조회 성공"),
    })
    public ResponseEntity<List<RepairPresetPartResponse>> getPresetParts(@PathVariable Long id) {
        return ResponseEntity.ok(repairPresetService.getPresetParts(id));
    }
}
