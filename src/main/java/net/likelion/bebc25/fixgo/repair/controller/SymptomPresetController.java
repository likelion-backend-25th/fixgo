package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.SymptomPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairPresetService;
import net.likelion.bebc25.fixgo.repair.service.SymptomPresetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Symptom Preset API", description = "고장 증상 프리셋 조회 및 연결 수리 프리셋 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/symptom-presets")
public class SymptomPresetController {

    private final SymptomPresetService symptomPresetService;
    private final RepairPresetService repairPresetService;

    // 고장 증상 프리셋 목록 조회
    @GetMapping
    @Operation(summary = "고장 증상 프리셋 목록 조회", description = "등록된 고장 증상 프리셋 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "증상 프리셋 목록 조회 성공"),
    })
    public ResponseEntity<List<SymptomPresetResponse>> getSymptomPresets() {
        return ResponseEntity.ok(symptomPresetService.getSymptomPresets());
    }

    // 고장 증상 프리셋 검색
    @GetMapping("/search")
    @Operation(summary = "고장 증상 프리셋 검색", description = "이름으로 고장 증상 프리셋을 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "증상 프리셋 검색 성공"),
    })
    public ResponseEntity<List<SymptomPresetResponse>> searchSymptomPresets(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(symptomPresetService.searchSymptomPresets(name));
    }

    // 증상에 연결된 수리 프리셋 조회
    @GetMapping("/{symptomId}/repair-presets")
    @Operation(summary = "증상에 연결된 수리 프리셋 조회", description = "고장 증상 ID에 연결된 수리 프리셋 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "연결 수리 프리셋 조회 성공"),
    })
    public ResponseEntity<List<RepairPresetResponse>> getRepairPresetsBySymptom(
            @PathVariable Long symptomId
    ) {
        return ResponseEntity.ok(repairPresetService.getPresetsBySymptom(symptomId));
    }

}
