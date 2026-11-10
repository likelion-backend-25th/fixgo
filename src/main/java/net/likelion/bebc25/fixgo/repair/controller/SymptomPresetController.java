package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.SymptomPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairPresetService;
import net.likelion.bebc25.fixgo.repair.service.SymptomPresetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/symptom-presets")
public class SymptomPresetController {

    private final SymptomPresetService symptomPresetService;
    private final RepairPresetService repairPresetService;

    // 고장 증상 프리셋 목록 조회
    @GetMapping
    public ResponseEntity<List<SymptomPresetResponse>> getSymptomPresets() {
        return ResponseEntity.ok(symptomPresetService.getSymptomPresets());
    }

    // 고장 증상 프리셋 검색
    @GetMapping("/search")
    public ResponseEntity<List<SymptomPresetResponse>> searchSymptomPresets(
            @RequestParam String name
    ) {
        return ResponseEntity.ok(symptomPresetService.searchSymptomPresets(name));
    }

    // 증상에 연결된 수리 프리셋 조회
    @GetMapping("/{symptomId}/repair-presets")
    public ResponseEntity<List<RepairPresetResponse>> getRepairPresetsBySymptom(
            @PathVariable Long symptomId
    ) {
        return ResponseEntity.ok(repairPresetService.getPresetsBySymptom(symptomId));
    }

}