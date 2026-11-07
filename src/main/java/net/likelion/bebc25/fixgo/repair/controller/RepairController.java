package net.likelion.bebc25.fixgo.repair.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repairs")
public class RepairController {

    private final RepairService repairService;

    // 수리 등록
    @PostMapping
    public ResponseEntity<RepairMessageResponse> createRepair(
            @Valid @RequestBody RepairRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairService.createRepair(dto, userDetails.getUsername()));
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

    // 본인에게 배정된 A/S 목록 조회
    @GetMapping("/my")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<RepairResponse>> getMyRepairs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(repairService.getMyRepairs(userDetails.getId()));
    }

    // 본인에게 배정된 A/S 상세 조회
    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<RepairResponse> getMyRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(repairService.getMyRepair(id, userDetails.getId()));
    }

    // 접수번호로 수리 상세 조회
    @GetMapping("/reception/{receptionNumber}")
    public ResponseEntity<RepairResponse> getRepairByReceptionNumber(
            @PathVariable String receptionNumber
    ) {
        return ResponseEntity.ok(repairService.getRepairByReceptionNumber(receptionNumber));
    }

    // 수리 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<RepairResponse> updateRepair(
            @PathVariable Long id,
            @Valid @RequestBody RepairRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repairService.updateRepair(id, dto, userDetails.getUsername())
        );
    }

    // 수리 상태 변경
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RepairMessageResponse> changeRepairStatus(
            @PathVariable Long id,
            @RequestBody Map<String, RepairStatus> request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepairStatus status = request.get("status");

        repairService.changeRepairStatus(id, status, userDetails.getUsername());

        return ResponseEntity.ok(
                new RepairMessageResponse("수리 상태가 변경되었습니다.")
        );
    }

    // 수리 시작
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<RepairMessageResponse> startRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairService.startRepair(id, userDetails.getId(), userDetails.getUsername());

        return ResponseEntity.ok(
                new RepairMessageResponse("수리가 시작되었습니다.")
        );
    }

    // 수리 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<RepairMessageResponse> deleteRepair(@PathVariable Long id) {

        repairService.deleteRepair(id);

        return ResponseEntity.ok(
                new RepairMessageResponse("수리 정보가 삭제되었습니다.")
        );
    }
}