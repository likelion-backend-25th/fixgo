package net.likelion.bebc25.fixgo.product.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.auth.principal.CustomUserDetails;
import net.likelion.bebc25.fixgo.product.dto.ProductModelDto;
import net.likelion.bebc25.fixgo.product.service.ProductModelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
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
        String createdBy = user.getMember().getCode();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productModelService.create(request, createdBy));
    }

    // 제품 검색시 해당되는 결과 반환
    @GetMapping
    public ResponseEntity<List<ProductModelDto.Response>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String modelName
    ) {
        return ResponseEntity.ok(
                productModelService.search(new ProductModelDto.SearchRequest(name, modelName))
        );
    }

    // 고객 정보 수정
    @PatchMapping("/{id}")
    public ResponseEntity<ProductModelDto.Response> update(
            @PathVariable Long id,
            @RequestBody ProductModelDto.UpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        String updatedBy = user.getMember().getCode();
        return ResponseEntity.ok(productModelService.update(id, request, updatedBy));
    }
}
