package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/as/v1/repairs")
public class RepairCostController {

    private final RepairCostService repairCostService;

    /**
     * 수리비 계산
     */
    @PostMapping("/{repairId}/cost")
    public ResponseEntity<Integer> calculateRepairCost(@PathVariable Long repairId, @AuthenticationPrincipal UserDetails userDetails) {
        int finalAmount = repairCostService.calculateRepairCost(repairId, userDetails.getUsername());

        return ResponseEntity.ok(finalAmount);
    }
}