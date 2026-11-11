package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairHistoryResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Repair History API", description = "A/S 수리 이력 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-histories")
public class RepairHistoryController {

    private final RepairHistoryService repairHistoryService;

    /**
     * A/S 수리 이력 조회
     */
    @GetMapping("/{asReceptionId}")
    @Operation(summary = "A/S 수리 이력 조회", description = "A/S 접수 ID로 수리 이력을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 이력 조회 성공"),
    })
    public ResponseEntity<RepairHistoryResponse> getRepairHistory(
            @PathVariable Long asReceptionId
    ) {
        return ResponseEntity.ok(
                repairHistoryService.getRepairHistory(asReceptionId)
        );
    }
}
