package net.likelion.bebc25.fixgo.as.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsReceptionDto;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.service.AsReceptionService;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/as-receptions")
public class AsReceptionController {

    private final AsReceptionService asReceptionService;

    // 접수 등록
    @PostMapping
    public ResponseEntity<AsReceptionDto.CreateReceptionResponse> createReception(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsReceptionDto.CreateReceptionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(asReceptionService.createReception(userDetails, request));
    }

    // 접수 목록 조회
    @GetMapping
    public ResponseEntity<Page<AsReceptionDto.ListResponse>> getReceptions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AsReceptionStatus status,
            @RequestParam(required = false) Long technicianId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                asReceptionService.getReceptions(
                        new AsReceptionDto.ReceptionSearchRequest(keyword, status, technicianId),
                        pageable
                )
        );
    }

    // 접수 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<AsReceptionDto.DetailResponse> getReception(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                asReceptionService.getReception(id)
        );
    }
}
