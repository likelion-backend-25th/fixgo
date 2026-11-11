package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Repair Cost API", description = "수리비 계산 및 확정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repairs")
public class RepairCostController {

    private final RepairCostService repairCostService;

    // 수리비 계산
    @PostMapping("/{repairId}/cost")
    @Operation(summary = "수리비 계산", description = "수리 ID로 수리비를 계산한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리비 계산 성공"),
    })
    public ResponseEntity<RepairCostResponse> calculateRepairCost(
            @PathVariable Long repairId
    ) {
        return ResponseEntity.ok(
                repairCostService.calculateRepairCost(repairId)
        );
    }

    // 수리비 확정
    @PostMapping("/{repairId}/cost/confirm")
    @Operation(summary = "수리비 확정", description = "계산된 수리비를 확정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리비 확정 성공"),
    })
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
