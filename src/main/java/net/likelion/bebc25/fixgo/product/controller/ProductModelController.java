package net.likelion.bebc25.fixgo.product.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.dto.ProductModelDto;
import net.likelion.bebc25.fixgo.product.service.ProductModelService;
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
@RequestMapping("/api/v1/admin/products")
public class ProductModelController {

    private final ProductModelService productModelService;

    // id로 ProductModelDto.Response 반환
    @GetMapping("/{id}")
    public ResponseEntity<ProductModelDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productModelService.getById(id));
    }

    // 제품 등록
    @PostMapping
    public ResponseEntity<ProductModelDto.Response> create(
            @RequestBody ProductModelDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long createdBy = user.getMember().getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productModelService.create(request, createdBy));
    }

    // 검색 결과 pagination
    @GetMapping("/list")
    public ResponseEntity<Page<ProductModelDto.Response>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "modelName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productModelService.search(keyword, pageable));
    }

    // 고객 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ProductModelDto.Response> update(
            @PathVariable Long id,
            @RequestBody ProductModelDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedBy = user.getMember().getId();
        return ResponseEntity.ok(productModelService.update(id, request, updatedBy));
    }
}
