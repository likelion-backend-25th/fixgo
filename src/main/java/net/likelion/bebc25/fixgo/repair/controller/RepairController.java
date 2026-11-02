package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequestDto;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponseDto;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/repairs")
public class RepairController {

    private final RepairService repairService;

    // 수리 등록
    @PostMapping
    public ResponseEntity<Map<String, String>> createRepair(@RequestBody RepairRequestDto dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(repairService.createRepair(dto));
    }

    // 전체 수리 목록 조회
    @GetMapping
    public ResponseEntity<List<RepairResponseDto>> getAllRepairs() {
        return ResponseEntity.ok(repairService.getAllRepairs());
    }

    // 수리 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RepairResponseDto> getRepair(@PathVariable Long id) {

        return ResponseEntity.ok(repairService.getRepair(id));
    }

    // 수리 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<RepairResponseDto> updateRepair(@PathVariable Long id, @RequestBody RepairRequestDto dto) {

        return ResponseEntity.ok(repairService.updateRepair(id, dto));
    }

    // 수리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteRepair(@PathVariable Long id) {

        repairService.deleteRepair(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "수리 정보가 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }
}
