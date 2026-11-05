package net.likelion.bebc25.fixgo.product.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.product.dto.ProductDto;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductModelRepository productModelRepository;

    // id로 product entity 조회(내부용)
    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 제품입니다."));
    }

    // 수리 대상 제품 정보 조회(Controller, Dto 반환용)
    public ProductDto.Response getById(Long id) {
        Product product = findProduct(id);
        ProductModel model = productModelRepository.findById(product.getProductModelId())
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 모델입니다."));
        return ProductDto.Response.from(product, model);
    }

    // 시리얼 번호로 제품 정보 조회
    public List<ProductDto.Response> search(ProductDto.SearchRequest request) {
        if (request.serialNumber() == null || request.serialNumber().isBlank()) {
            return List.of();
        }
        return productRepository.findBySerialNumber(request.serialNumber())
                .stream()
                .map(product -> {
                    ProductModel model = productModelRepository.findById(product.getProductModelId())
                            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 모델입니다."));
                    return ProductDto.Response.from(product, model);
                })
                .toList();
    }
}
