package net.likelion.bebc25.fixgo.as.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.EstimateResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as-receptions")
public class AsReceptionController {

    private final AsReceptionService asReceptionService;

    private final AsStatusHistoryService asStatusHistoryService;

    private final RepairCostService repairCostService;

    // 접수 등록
    @PostMapping
    public ResponseEntity<AsReceptionDto.CreateReceptionResponse> createReception(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsReceptionDto.CreateReceptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asReceptionService.createReception(userDetails, request));
    }

    // 접수 목록 조회
    @GetMapping
    public ResponseEntity<Page<AsReceptionDto.ListResponse>> getReceptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AsReceptionStatus status,
            @RequestParam(required = false) Long technicianId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AsReceptionDto.ReceptionSearchRequest request =
                new AsReceptionDto.ReceptionSearchRequest(
                        keyword,
                        status,
                        technicianId
                );

        return ResponseEntity.ok(asReceptionService.getReceptions(request, pageable));
    }

    // 접수 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AsReceptionDto.DetailResponse> getReception(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(asReceptionService.getReception(id));
    }

    // 접수 수정
    @PatchMapping("/{id}")
    public ResponseEntity<AsReceptionDto.DetailResponse> updateReception(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsReceptionDto.UpdateReceptionRequest request
    ) {
        return ResponseEntity.ok(asReceptionService.updateReception(id, userDetails, request));
    }

    // 접수 상태 변경 (내 admin 컨트롤러에서 이동)
    @PatchMapping("/{receptionId}/status")   // ← 원래 admin 파일의 매핑 줄로 교체
    public void changeReceptionStatus(
            @PathVariable Long receptionId,
            @RequestParam AsReceptionStatus newStatus,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        asReceptionService.changeReceptionStatus(receptionId, newStatus, reason, userDetails);
    }

    // 접수 상태 변경 이력 조회
    @GetMapping("/{id}/history")
    public List<AsStatusHistoryDto> getHistory(
            @PathVariable Long id
    ) {
        return asStatusHistoryService.getHistory(id);
    }

    // 기사 재배정
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @PatchMapping("/{receptionId}/technician")
    public ResponseEntity<Void> updateTechnician(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long receptionId,
            @Valid @RequestBody AsReceptionDto.updateTechnicianRequest request
    ) {
        asReceptionService.updateTechnician(
                receptionId,
                request.technicianId(),
                request.reason(),
                userDetails.getMember()
        );
        return ResponseEntity.noContent().build();
    }

    // 기사 배정 이력 조회
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{receptionId}/technician-history")
    public List<AsReceptionDto.TechnicianAssignmentHistoryResponse> getTechnicianAssignmentHistory(
            @PathVariable Long receptionId
    ) {
        return asReceptionService.getTechnicianAssignmentHistory(receptionId);
    }

    // 견적서 조회
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{id}/documents/estimate")
    public ResponseEntity<EstimateResponse> getEstimate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(repairCostService.getEstimate(id));
    }

}
