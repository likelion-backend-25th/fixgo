package net.likelion.bebc25.fixgo.product.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.product.dto.ProductDto;
import net.likelion.bebc25.fixgo.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    // 수리 대상 제품 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
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
}