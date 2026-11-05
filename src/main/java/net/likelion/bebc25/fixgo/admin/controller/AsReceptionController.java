package net.likelion.bebc25.fixgo.admin.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.as.service.AsStatusHistoryService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as")
public class AsReceptionController {

    private final AsReceptionService asReceptionService;
    private final AsStatusHistoryService asStatusHistoryService;

    // A/S 접수 등록
    @PostMapping
    public AsReceptionDto.DetailResponse createReception(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AsReceptionDto.CreateReceptionRequest request) {
        return asReceptionService.createReception(userDetails, request);
    }

    // A/S 접수 상태 변경
    @PatchMapping("/{receptionId}/status")
    public void changeReceptionStatus(@PathVariable Long receptionId, @RequestParam AsReceptionStatus newStatus, @RequestParam(required = false) String reason, @AuthenticationPrincipal CustomUserDetails userDetails) {
        asReceptionService.changeReceptionStatus(receptionId, newStatus, reason, userDetails);
    }

    // A/S 상태 변경 이력 조회
    @GetMapping("/{receptionId}/history")
    public ResponseEntity<List<AsStatusHistoryDto>> getHistory(@PathVariable Long receptionId) {
        return ResponseEntity.ok(asStatusHistoryService.getHistory(receptionId));
    }
}
