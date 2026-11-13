package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.part.PartDto;
import net.likelion.bebc25.fixgo.repair.service.PartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Part API", description = "부품 조회 및 단가·재고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/part")
public class PartController {
    private final PartService partService;

    @GetMapping
    @Operation(summary = "부품 목록 조회", description = "검색 조건에 맞는 부품 목록을 페이징하여 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부품 목록 조회 성공"),
    })
    public ResponseEntity<Page<PartDto.BriefResponse>> searchParts(
            @ModelAttribute PartDto.SearchRequest request,   // @RequestBody → @ModelAttribute
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(partService.searchParts(request, pageable));
    }

    @GetMapping("/{partId}")
    @Operation(summary = "부품 상세 조회", description = "부품 ID로 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부품 상세 조회 성공"),
    })
    public ResponseEntity<PartDto.DetailResponse> getPartsById(
            @PathVariable Long partId
    ) {
        return ResponseEntity.ok(partService.getPartsById(partId));
    }

    @PatchMapping("/unit_price")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "부품 단가 수정", description = "관리자가 부품 단가를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부품 단가 수정 성공"),
    })
    public ResponseEntity<Void> updateUnitPrice(
            @RequestBody PartDto.PriceUpdateRequest request
    ) {
        partService.updateUnitPrice(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/standard_minutes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "표준 작업시간 수정", description = "관리자가 부품 표준 작업시간(분)을 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "표준 작업시간 수정 성공"),
    })
    public ResponseEntity<Void> updateStandardMinutes(
            @RequestBody PartDto.StandardMinutesUpdateRequest request
    ) {
        partService.updateStandardMinutes(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "부품 상태 수정", description = "사용·재고 부족 부품을 단종하거나, 단종 부품을 이전 상태로 되돌린다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "부품 상태 수정 성공"),
    })
    public ResponseEntity<Void> updateStatus(
            @RequestBody PartDto.StatusUpdateRequest request
    ) {
        partService.updateStatus(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/stock")
    @Operation(summary = "재고 수량 단건 수정", description = "단일 부품의 재고 수량을 입출고 처리한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재고 수량 수정 성공"),
    })
    public ResponseEntity<Void> updateSingleStockQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PartDto.SingleStockUpdateRequest request
    ) {
        partService.updateSingleStockQuantity(userDetails, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/stocks")
    @Operation(summary = "재고 수량 다건 수정", description = "수리 ID에 연결된 사용 부품들의 재고 수량을 일괄 반영한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재고 수량 일괄 수정 성공"),
    })
    public ResponseEntity<Void> updateMultiStockQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long repairId
    ) {
        partService.updateMultiStockQuantity(userDetails, repairId);
        return ResponseEntity.ok().build();
    }
}
