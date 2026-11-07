package net.likelion.bebc25.fixgo.repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
import net.likelion.bebc25.fixgo.repair.service.RepairWorkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-works")
public class RepairWorkController {

    private final RepairWorkService repairWorkService;

    // 수리 작업 내역 등록
    @PostMapping
    public ResponseEntity<RepairWorkResponse> createRepairWork(
            @Valid @RequestBody RepairWorkRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairWorkService.createRepairWork(dto, userDetails.getUsername()));
    }

    // 수리 작업 내역 전체 조회
    @GetMapping
    public ResponseEntity<List<RepairWorkResponse>> getAllRepairWorks() {
        return ResponseEntity.ok(repairWorkService.getAllRepairWorks());
    }

    // 수리 작업 내역 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<RepairWorkResponse> getRepairWork(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(repairWorkService.getRepairWork(id));
    }

    // 특정 수리 건의 작업 내역 조회
    @GetMapping("/repair/{repairId}")
    public ResponseEntity<List<RepairWorkResponse>> getRepairWorksByRepairId(
            @PathVariable Long repairId
    ) {
        return ResponseEntity.ok(
                repairWorkService.getRepairWorksByRepairId(repairId)
        );
    }

    // 작업명 검색
    @GetMapping("/search")
    public ResponseEntity<List<RepairWorkResponse>> searchRepairWorks(
            @RequestParam String workName
    ) {
        return ResponseEntity.ok(repairWorkService.searchRepairWorks(workName));
    }

    // 수리 작업 내역 수정
    @PutMapping("/{id}")
    public ResponseEntity<RepairWorkResponse> updateRepairWork(
            @PathVariable Long id,
            @Valid @RequestBody RepairWorkUpdate dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repairWorkService.updateRepairWork(id, dto, userDetails.getUsername())
        );
    }

    // 수리 작업 내역 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairWork(
            @PathVariable Long id
    ) {
        repairWorkService.deleteRepairWork(id);

        return ResponseEntity.noContent().build();
    }
}