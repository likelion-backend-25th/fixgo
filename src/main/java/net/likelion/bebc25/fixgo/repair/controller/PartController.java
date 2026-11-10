package net.likelion.bebc25.fixgo.repair.controller;

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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/part")
public class PartController {
    private final PartService partService;

    @GetMapping
    public ResponseEntity<Page<PartDto.BriefResponse>> searchParts(
            @ModelAttribute PartDto.SearchRequest request,   // @RequestBody → @ModelAttribute
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(partService.searchParts(request, pageable));
    }

    @GetMapping("/{partId}")
    public ResponseEntity<PartDto.DetailResponse> getPartsById(
            @PathVariable Long partId
    ) {
        return ResponseEntity.ok(partService.getPartsById(partId));
    }

    @PatchMapping("/unit_price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUnitPrice(
            @RequestBody PartDto.PriceUpdateRequest request
    ) {
        partService.updateUnitPrice(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/standard_minutes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStandardMinutes(
            @RequestBody PartDto.StandardMinutesUpdateRequest request
    ) {
        partService.updateStandardMinutes(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/stock")
    public ResponseEntity<Void> updateSingleStockQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PartDto.SingleStockUpdateRequest request
    ) {
        partService.updateSingleStockQuantity(userDetails, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/stocks")
    public ResponseEntity<Void> updateMultiStockQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long repairId
    ) {
        partService.updateMultiStockQuantity(userDetails, repairId);
        return ResponseEntity.ok().build();
    }
}
