package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPartUsageResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairPartUsageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Repair Part Usage API", description = "실제 수리 부품 사용 등록 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-part-usages")
public class RepairPartUsageController {

    private final RepairPartUsageService repairPartUsageService;

    /**
     * 실제 수리 부품 사용 등록
     */
    @PostMapping
    @Operation(summary = "실제 수리 부품 사용 등록", description = "수리에 사용된 부품을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "부품 사용 등록 성공"),
    })
    public ResponseEntity<RepairPartUsageResponse> createUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RepairPartUsageRequest request
    ) {

        RepairPartUsageResponse response =
                repairPartUsageService.createUsage(userDetails, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
