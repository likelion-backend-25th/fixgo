package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairCostResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repairs")
public class RepairCostController {

    private final RepairCostService repairCostService;

    // 수리비 계산
    @PostMapping("/{repairId}/cost")
    public ResponseEntity<RepairCostResponse> calculateRepairCost(
            @PathVariable Long repairId
    ) {
        return ResponseEntity.ok(
                repairCostService.calculateRepairCost(repairId)
        );
    }

    // 수리비 확정
    @PostMapping("/{repairId}/cost/confirm")
    public ResponseEntity<RepairMessageResponse> confirmRepairCost(
            @PathVariable Long repairId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairCostService.confirmRepairCost(repairId, userDetails);

        return ResponseEntity.ok(
                new RepairMessageResponse("수리비가 확정되었습니다.")
        );
    }
}