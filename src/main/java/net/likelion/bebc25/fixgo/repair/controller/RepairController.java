package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
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
    public ResponseEntity<Map<String, String>> createRepair(@RequestBody RepairRequest dto) {

        return ResponseEntity.status(HttpStatus.CREATED).body(repairService.createRepair(dto));
    }

    // 전체 수리 목록 조회
    @GetMapping
    public ResponseEntity<List<RepairResponse>> getAllRepairs() {
        return ResponseEntity.ok(repairService.getAllRepairs());
    }

    // 수리 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<RepairResponse> getRepair(@PathVariable Long id) {

        return ResponseEntity.ok(repairService.getRepair(id));
    }

    // 수리 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<RepairResponse> updateRepair(@PathVariable Long id, @RequestBody RepairRequest dto) {

        return ResponseEntity.ok(repairService.updateRepair(id, dto));
    }

    // 수리 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> changeRepairStatus(@PathVariable Long id, @RequestBody Map<String, RepairStatus> request) {

        RepairStatus status = request.get("status");

        repairService.changeRepairStatus(id, status);

        Map<String, String> response = new HashMap<>();
        response.put("message", "수리 상태가 변경되었습니다.");

        return ResponseEntity.ok(response);
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
