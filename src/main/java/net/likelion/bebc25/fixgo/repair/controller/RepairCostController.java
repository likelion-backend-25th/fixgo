package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repairs")
public class RepairCostController {

    private final RepairCostService repairCostService;

    /**
     * 수리비 계산
     */
    @PostMapping("/{repairId}/cost")
    public ResponseEntity<Long> calculateRepairCost(
            @PathVariable Long repairId
    ) {
        Long finalAmount = repairCostService.calculateRepairCost(repairId);

        return ResponseEntity.ok(finalAmount);
    }

    /**
     * 수리비 확정
     */
    @PostMapping("/{repairId}/cost/confirm")
    public ResponseEntity<Map<String, String>> confirmRepairCost(
            @PathVariable Long repairId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairCostService.confirmRepairCost(repairId, userDetails);

        return ResponseEntity.ok(
                Map.of("message", "수리비가 확정되었습니다.")
        );
    }
}
