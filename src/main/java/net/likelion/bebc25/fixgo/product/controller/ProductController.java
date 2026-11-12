package net.likelion.bebc25.fixgo.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Product API", description = "수리 대상 제품 조회/등록/수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    // 수리 대상 제품 정보 조회
    @GetMapping("/{id}")
    @Operation(summary = "수리 대상 제품 정보 조회", description = "제품 ID로 수리 대상 제품 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 정보 조회 성공"),
    })
    public ResponseEntity<ProductDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // 수리 대상 제품 정보 등록
    @PostMapping
    @Operation(summary = "수리 대상 제품 정보 등록", description = "수리 대상 제품 정보를 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "제품 정보 등록 성공"),
    })
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
    @Operation(summary = "시리얼번호 검색", description = "시리얼번호로 수리 대상 제품을 검색한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "시리얼번호 검색 성공"),
    })
    public ResponseEntity<List<ProductDto.Response>> search(
            @RequestParam(required = false) String serialNumber
    ) {
        return ResponseEntity.ok(
                productService.search(new ProductDto.SearchRequest(serialNumber))
        );
    }

    @GetMapping("/search")
    @Operation(summary = "제품 페이징 검색", description = "시리얼번호 조건으로 제품을 페이징 검색한다. 시리얼번호가 없으면 전체를 페이징한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 페이징 검색 성공"),
    })
    public ResponseEntity<Page<ProductDto.Response>> searchPage(
            @ModelAttribute ProductDto.SearchRequest request,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(productService.searchPage(request, pageable));
    }


    // 수리 대상 제품 정보 수정
    @PatchMapping("/{id}")
    @Operation(summary = "수리 대상 제품 정보 수정", description = "수리 대상 제품 정보를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 정보 수정 성공"),
    })
    public ResponseEntity<ProductDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedBy = user.getMember().getId();
        return ResponseEntity.ok(productService.update(id, request, updatedBy));
    }
}
