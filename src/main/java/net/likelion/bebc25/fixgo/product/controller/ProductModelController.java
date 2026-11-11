package net.likelion.bebc25.fixgo.product.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "Product Model API", description = "제품 모델 조회/등록/수정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/products")
public class ProductModelController {

    private final ProductModelService productModelService;

    // id로 ProductModelDto.Response 반환
    @GetMapping("/{id}")
    @Operation(summary = "제품 모델 단건 조회", description = "id로 ProductModelDto.Response를 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 모델 조회 성공"),
    })
    public ResponseEntity<ProductModelDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productModelService.getById(id));
    }

    // 제품 등록
    @PostMapping
    @Operation(summary = "제품 모델 등록", description = "제품 모델을 등록한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "제품 모델 등록 성공"),
    })
    public ResponseEntity<ProductModelDto.Response> create(
            @Valid @RequestBody ProductModelDto.CreateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long createdBy = user.getMember().getId();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productModelService.create(request, createdBy));
    }

    // 검색 결과 pagination
    @GetMapping("/list")
    @Operation(summary = "제품 모델 목록 검색", description = "키워드로 제품 모델을 검색하고 페이징한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 모델 목록 검색 성공"),
    })
    public ResponseEntity<Page<ProductModelDto.Response>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "modelName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(productModelService.search(keyword, pageable));
    }

    // 고객 정보 수정
    @PatchMapping("/{id}")
    @Operation(summary = "제품 모델 수정", description = "제품 모델 정보를 수정한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "제품 모델 수정 성공"),
    })
    public ResponseEntity<ProductModelDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductModelDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long updatedBy = user.getMember().getId();
        return ResponseEntity.ok(productModelService.update(id, request, updatedBy));
    }
}
