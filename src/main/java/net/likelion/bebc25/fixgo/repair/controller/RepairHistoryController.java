package net.likelion.bebc25.fixgo.repair.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-histories")
public class RepairHistoryController {

    private final RepairHistoryService repairHistoryService;

    /**
     * A/S 수리 이력 조회
     */
    @GetMapping("/{asReceptionId}")
    public ResponseEntity<RepairHistoryResponse> getRepairHistory(
            @PathVariable Long asReceptionId
    ) {
        return ResponseEntity.ok(
                repairHistoryService.getRepairHistory(asReceptionId)
        );
    }
}