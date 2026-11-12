package net.likelion.bebc25.fixgo.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.admin.dto.LaborRateDto;
import net.likelion.bebc25.fixgo.admin.service.LaborRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Labor Rate API", description = "공임비·점검비 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/labor-rates")
public class LaborRateController {

    private final LaborRateService laborRateService;

    @GetMapping
    @Operation(summary = "공임비·점검비 전체 목록 조회", description = "등록된 공임비·점검비 전체 목록을 한 번에 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공임비·점검비 전체 목록 조회 성공"),
    })
    public ResponseEntity<List<LaborRateDto.Response>> getAllLaborRates() {
        return ResponseEntity.ok(laborRateService.getAllLaborRates());
    }

    @GetMapping("/{id}")
    @Operation(summary = "공임비·점검비 상세 조회", description = "요금 ID로 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공임비·점검비 상세 조회 성공"),
    })
    public ResponseEntity<LaborRateDto.Response> getLaborRate(@PathVariable Long id) {
        return ResponseEntity.ok(laborRateService.getLaborRate(id));
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "공임비·점검비 수정", description = "관리자가 기존 공임비 또는 점검비 금액을 수정한다. 적용일은 수정 당일로 자동 반영된다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공임비·점검비 수정 성공"),
    })
    public ResponseEntity<LaborRateDto.Response> updateLaborRate(
            @RequestBody @Valid LaborRateDto.UpdateRequest request
    ) {
        return ResponseEntity.ok(laborRateService.updateLaborRate(request));
    }
}
