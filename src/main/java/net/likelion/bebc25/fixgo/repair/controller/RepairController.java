package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairAbortRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairMessageResponse;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairRequest;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Repair API", description = "수리 등록·조회·상태 변경 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repairs")
public class RepairController {

    private final RepairService repairService;

    // 수리 등록
    @PostMapping
    @Operation(summary = "수리 등록", description = "새로운 수리 정보를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "수리 등록 성공"),
    })
    public ResponseEntity<RepairMessageResponse> createRepair(
            @Valid @RequestBody RepairRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairService.createRepair(dto, userDetails.getUsername()));
    }

    // 전체 수리 목록 조회
    @GetMapping
    @Operation(summary = "전체 수리 목록 조회", description = "등록된 전체 수리 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 목록 조회 성공"),
    })
    public ResponseEntity<List<RepairResponse>> getAllRepairs() {
        return ResponseEntity.ok(repairService.getAllRepairs());
    }

    // 수리 상세 조회
    @GetMapping("/{id}")
    @Operation(summary = "수리 상세 조회", description = "수리 ID로 수리 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 상세 조회 성공"),
    })
    public ResponseEntity<RepairResponse> getRepair(@PathVariable Long id) {
        return ResponseEntity.ok(repairService.getRepair(id));
    }

    // 본인에게 배정된 A/S 목록 조회
    @GetMapping("/my")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "본인에게 배정된 A/S 목록 조회", description = "담당 기사에게 배정된 수리 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배정 수리 목록 조회 성공"),
    })
    public ResponseEntity<List<RepairResponse>> getMyRepairs(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(repairService.getMyRepairs(userDetails.getId()));
    }

    // 본인에게 배정된 A/S 상세 조회
    @GetMapping("/my/{id}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "본인에게 배정된 A/S 상세 조회", description = "담당 기사에게 배정된 수리 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "배정 수리 상세 조회 성공"),
    })
    public ResponseEntity<RepairResponse> getMyRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(repairService.getMyRepair(id, userDetails.getId()));
    }

    // 접수번호로 수리 상세 조회
    @GetMapping("/reception/{receptionNumber}")
    @Operation(summary = "접수번호로 수리 상세 조회", description = "A/S 접수번호로 수리 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 상세 조회 성공"),
    })
    public ResponseEntity<RepairResponse> getRepairByReceptionNumber(
            @PathVariable String receptionNumber
    ) {
        return ResponseEntity.ok(repairService.getRepairByReceptionNumber(receptionNumber));
    }

    // 수리 정보 수정
    @PutMapping("/{id}")
    @Operation(summary = "수리 정보 수정", description = "수리 정보를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 정보 수정 성공"),
    })
    public ResponseEntity<RepairResponse> updateRepair(
            @PathVariable Long id,
            @Valid @RequestBody RepairRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repairService.updateRepair(id, dto, userDetails.getUsername())
        );
    }

    // 수리 상태 변경
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "수리 상태 변경", description = "관리자/매니저가 수리 상태를 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 상태 변경 성공"),
    })
    public ResponseEntity<RepairMessageResponse> changeRepairStatus(
            @PathVariable Long id,
            @RequestBody Map<String, RepairStatus> request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        RepairStatus status = request.get("status");

        repairService.changeRepairStatus(id, status, userDetails.getUsername());

        return ResponseEntity.ok(
                new RepairMessageResponse("수리 상태가 변경되었습니다.")
        );
    }

    // 수리 시작
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "수리 시작", description = "담당 기사가 수리를 시작한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 시작 성공"),
    })
    public ResponseEntity<RepairMessageResponse> startRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairService.startRepair(id, userDetails.getId(), userDetails.getUsername());

        return ResponseEntity.ok(
                new RepairMessageResponse("수리가 시작되었습니다.")
        );
    }

    // 수리 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "수리 삭제", description = "수리 정보를 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 삭제 성공"),
    })
    public ResponseEntity<RepairMessageResponse> deleteRepair(@PathVariable Long id) {

        repairService.deleteRepair(id);

        return ResponseEntity.ok(
                new RepairMessageResponse("수리 정보가 삭제되었습니다.")
        );
    }

    // 점검 중 A/S 중단. 배송 또는 수령 대기로 넘긴다.
    @PostMapping("/{id}/abort")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "A/S 중단", description = "담당 기사가 점검 중인 A/S를 중단하고 반환 대기로 넘긴다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/S 중단 성공"),
    })
    public ResponseEntity<RepairMessageResponse> abortInspection(
            @PathVariable Long id,
            @Valid @RequestBody RepairAbortRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairService.abortInspection(id, request, userDetails);

        return ResponseEntity.ok(
                new RepairMessageResponse("A/S를 중단하고 반환 대기로 넘겼습니다.")
        );
    }

    // 수리 완료
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('TECHNICIAN')")
    @Operation(summary = "수리 완료", description = "담당 기사가 본인에게 배정된 수리를 완료한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 완료 성공"),
    })
    public ResponseEntity<RepairMessageResponse> completeMyRepair(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        repairService.completeRepair(id, userDetails);

        return ResponseEntity.ok(
                new RepairMessageResponse("수리가 완료되었습니다.")
        );
    }
}
