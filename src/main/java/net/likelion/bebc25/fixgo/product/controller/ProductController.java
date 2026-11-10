package net.likelion.bebc25.fixgo.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.dto.ProductDto;
import net.likelion.bebc25.fixgo.product.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    // 고객 보유 제품 목록 조회
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<ProductDto.Response>> listByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(productService.listByCustomerId(customerId));
    }

    // 수리 대상 제품 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // 수리 대상 제품 정보 등록
    @PostMapping
    public ResponseEntity<ProductDto.Response> create(
            @Valid @RequestBody ProductDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long createdBy = user.getMember().getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request, createdBy));
    }

    // 시리얼번호 검색
    @GetMapping
    public ResponseEntity<List<ProductDto.Response>> search(
            @RequestParam(required = false) String serialNumber
    ) {
        return ResponseEntity.ok(
                productService.search(new ProductDto.SearchRequest(serialNumber))
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto.Response>> searchPage(
            @ModelAttribute ProductDto.SearchRequest request,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchPage(request, pageable));
    }


    // 수리 대상 제품 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedBy = user.getMember().getId();
        return ResponseEntity.ok(productService.update(id, request, updatedBy));
    }
}
