package net.likelion.bebc25.fixgo.repair.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkRequest;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkResponse;
import net.likelion.bebc25.fixgo.repair.dto.repairWork.RepairWorkUpdate;
import net.likelion.bebc25.fixgo.repair.service.RepairWorkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Repair Work API", description = "수리 작업 내역 등록·조회·수정·삭제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as/repair-works")
public class RepairWorkController {

    private final RepairWorkService repairWorkService;

    // 수리 작업 내역 등록
    @PostMapping
    @Operation(summary = "수리 작업 내역 등록", description = "새로운 수리 작업 내역을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "수리 작업 내역 등록 성공"),
    })
    public ResponseEntity<RepairWorkResponse> createRepairWork(
            @Valid @RequestBody RepairWorkRequest dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairWorkService.createRepairWork(dto, userDetails.getUsername()));
    }

    // 수리 작업 내역 전체 조회
    @GetMapping
    @Operation(summary = "수리 작업 내역 전체 조회", description = "등록된 전체 수리 작업 내역을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 작업 내역 목록 조회 성공"),
    })
    public ResponseEntity<List<RepairWorkResponse>> getAllRepairWorks() {
        return ResponseEntity.ok(repairWorkService.getAllRepairWorks());
    }

    // 수리 작업 내역 단건 조회
    @GetMapping("/{id}")
    @Operation(summary = "수리 작업 내역 단건 조회", description = "수리 작업 내역 ID로 단건을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 작업 내역 단건 조회 성공"),
    })
    public ResponseEntity<RepairWorkResponse> getRepairWork(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(repairWorkService.getRepairWork(id));
    }

    // 특정 수리 건의 작업 내역 조회
    @GetMapping("/repair/{repairId}")
    @Operation(summary = "특정 수리 건의 작업 내역 조회", description = "수리 ID에 연결된 작업 내역 목록을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 건 작업 내역 조회 성공"),
    })
    public ResponseEntity<List<RepairWorkResponse>> getRepairWorksByRepairId(
            @PathVariable Long repairId
    ) {
        return ResponseEntity.ok(
                repairWorkService.getRepairWorksByRepairId(repairId)
        );
    }

    // 작업명 검색
    @GetMapping("/search")
    @Operation(summary = "작업명 검색", description = "작업명으로 수리 작업 내역을 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작업명 검색 성공"),
    })
    public ResponseEntity<List<RepairWorkResponse>> searchRepairWorks(
            @RequestParam String workName
    ) {
        return ResponseEntity.ok(repairWorkService.searchRepairWorks(workName));
    }

    // 수리 작업 내역 수정
    @PutMapping("/{id}")
    @Operation(summary = "수리 작업 내역 수정", description = "수리 작업 내역을 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수리 작업 내역 수정 성공"),
    })
    public ResponseEntity<RepairWorkResponse> updateRepairWork(
            @PathVariable Long id,
            @Valid @RequestBody RepairWorkUpdate dto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                repairWorkService.updateRepairWork(id, dto, userDetails.getUsername())
        );
    }

    // 수리 작업 내역 삭제
    @DeleteMapping("/{id}")
    @Operation(summary = "수리 작업 내역 삭제", description = "수리 작업 내역을 삭제한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "수리 작업 내역 삭제 성공"),
    })
    public ResponseEntity<Void> deleteRepairWork(
            @PathVariable Long id
    ) {
        repairWorkService.deleteRepairWork(id);

        return ResponseEntity.noContent().build();
    }
}
