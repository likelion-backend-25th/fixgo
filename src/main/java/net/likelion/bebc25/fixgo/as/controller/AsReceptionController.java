package net.likelion.bebc25.fixgo.as.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsHistoryListDto;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.portal.dto.CustomerPortalLinkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.EstimateResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairStatementResponse;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import net.likelion.bebc25.fixgo.repair.service.RepairHistoryService;
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

@Tag(name = "A/S Reception API", description = "A/S 접수 등록·조회·상태 변경 및 문서 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as-receptions")
public class AsReceptionController {

    private final AsReceptionService asReceptionService;
    private final AsStatusHistoryService asStatusHistoryService;
    private final RepairCostService repairCostService;
    private final RepairHistoryService repairHistoryService;

    // 접수 등록
    @PostMapping
    @Operation(summary = "접수 등록", description = "새로운 A/S 접수를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "접수 등록 성공"),
    })
    public ResponseEntity<AsReceptionDto.CreateReceptionResponse> createReception(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsReceptionDto.CreateReceptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asReceptionService.createReception(userDetails, request));
    }

    // 전체 상태 변경 이력. 접수별 조회와 달리 관리자 화면의 목록에 쓴다.
    @GetMapping("/status-histories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AsHistoryListDto.StatusItem>> getStatusHistories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String toStatus,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(asStatusHistoryService.search(keyword, toStatus, pageable));
    }

    // 전체 기사 배정 이력
    @GetMapping("/technician-histories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AsHistoryListDto.TechnicianItem>> getTechnicianHistories(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(asReceptionService.searchTechnicianHistory(keyword, pageable));
    }

    // 접수 목록 조회
    @GetMapping
    @Operation(summary = "접수 목록 조회", description = "검색 조건에 맞는 A/S 접수 목록을 페이징하여 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "접수 목록 조회 성공"),
    })
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
    @Operation(summary = "접수 상세 조회", description = "접수 ID로 A/S 접수 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "접수 상세 조회 성공"),
    })
    public ResponseEntity<AsReceptionDto.DetailResponse> getReception(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(asReceptionService.getReception(id));
    }

    // 접수 수정
    @PatchMapping("/{id}")
    @Operation(summary = "접수 수정", description = "A/S 접수 정보를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "접수 수정 성공"),
    })
    public ResponseEntity<AsReceptionDto.DetailResponse> updateReception(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsReceptionDto.UpdateReceptionRequest request
    ) {
        return ResponseEntity.ok(asReceptionService.updateReception(id, userDetails, request));
    }

    // 접수 상태 수동 변경
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    @PatchMapping("/{receptionId}/status")
    @Operation(summary = "접수 상태 수동 변경", description = "관리자/접수원이 A/S 접수 상태를 수동으로 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "접수 상태 변경 성공"),
    })
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
    @Operation(summary = "접수 상태 변경 이력 조회", description = "A/S 접수의 상태 변경 이력을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 이력 조회 성공"),
    })
    public List<AsStatusHistoryDto> getHistory(
            @PathVariable Long id
    ) {
        return asStatusHistoryService.getHistory(id);
    }

    // 기사 재배정
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @PatchMapping("/{receptionId}/technician")
    @Operation(summary = "기사 재배정", description = "접수원/관리자가 A/S 접수의 담당 기사를 재배정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "기사 재배정 성공"),
    })
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
    @Operation(summary = "기사 배정 이력 조회", description = "A/S 접수의 기사 배정/재배정 이력을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기사 배정 이력 조회 성공"),
    })
    public List<AsReceptionDto.TechnicianAssignmentHistoryResponse> getTechnicianAssignmentHistory(
            @PathVariable Long receptionId
    ) {
        return asReceptionService.getTechnicianAssignmentHistory(receptionId);
    }

    // 고객 안내 링크 조회
    @GetMapping("/{id}/portal-link")
    @Operation(summary = "고객 안내 링크 조회", description = "고객 포탈 안내 링크를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "고객 안내 링크 조회 성공"),
    })
    public ResponseEntity<CustomerPortalLinkResponse> getPortalLink(@PathVariable Long id) {
        return ResponseEntity.ok(asReceptionService.getPortalLink(id));
    }

    // 접수증 조회
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{id}/documents/reception-receipt")
    @Operation(summary = "접수증 조회", description = "A/S 접수증 문서를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "접수증 조회 성공"),
    })
    public ResponseEntity<AsReceptionDto.ReceptionReceiptResponse> getReceptionReceipt(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(asReceptionService.getReceptionReceipt(id));
    }

    // 견적서 조회
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{id}/documents/estimate")
    @Operation(summary = "견적서 조회", description = "A/S 접수에 대한 견적서를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "견적서 조회 성공"),
    })
    public ResponseEntity<EstimateResponse> getEstimate(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(repairCostService.getEstimate(id));
    }

    // 수리내역서 조회
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    @GetMapping("/{id}/documents/repair-statement")
    @Operation(summary = "수리내역서 조회", description = "A/S 접수에 대한 수리내역서를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리내역서 조회 성공"),
    })
    public ResponseEntity<RepairStatementResponse> getRepairStatement(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(repairHistoryService.getRepairStatement(id));
    }

    // 기사 전용 접수 상세 조회
    @GetMapping("/my")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "기사 전용 접수 목록 조회", description = "담당 기사에게 배정된 A/S 접수 목록을 페이징하여 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기사 전용 접수 목록 조회 성공"),
    })
    public ResponseEntity<Page<AsReceptionDto.ListResponse>> getMyReceptions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AsReceptionStatus status,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var request = new AsReceptionDto.ReceptionSearchRequest(
                keyword, status, userDetails.getId()
        );
        return ResponseEntity.ok(asReceptionService.getReceptions(request, pageable));
    }

    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "기사 전용 접수 상세 조회", description = "담당 기사에게 배정된 A/S 접수 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기사 전용 접수 상세 조회 성공"),
    })
    public ResponseEntity<AsReceptionDto.MyReceptionDetailResponse> getMyReception(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                asReceptionService.getMyReception(id, userDetails.getId())
        );
    }

    // 담당 기사가 접수 건의 점검을 시작한다.
    @PostMapping("/my/{id}/inspection/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "점검 시작", description = "담당 기사가 접수 건의 점검을 시작한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "점검 시작 성공"),
    })
    public ResponseEntity<RepairResponse> startMyInspection(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                asReceptionService.startMyInspection(id, userDetails.getId())
        );
    }

}
