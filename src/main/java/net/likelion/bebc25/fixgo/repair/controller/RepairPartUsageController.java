package net.likelion.bebc25.fixgo.repair.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-part-usages")
public class RepairPartUsageController {

    private final RepairPartUsageService repairPartUsageService;

    /**
     * 실제 수리 부품 사용 등록
     */
    @PostMapping
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