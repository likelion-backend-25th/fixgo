package net.likelion.bebc25.fixgo.product.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.product.dto.ProductDto;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import net.likelion.bebc25.fixgo.product.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private ProductModel findProductModel(Long productModelId) {
        return productModelRepository.findById(productModelId)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 모델입니다."));
    }

    private ProductDto.Response toResponse(Product product) {
        return ProductDto.Response.from(product, findProductModel(product.getProductModelId()));
    }

    // 수리 대상 제품 정보 조회(Controller, Dto 반환용)
    public ProductDto.Response getById(Long id) {
        return toResponse(findProduct(id));
    }

    // 수리 대상 제품 등록
    @Transactional
    public ProductDto.Response create(ProductDto.CreateRequest request, Long createdBy) {
        findProductModel(request.productModelId());
        if (productRepository.existsBySerialNumber(request.serialNumber())) {
            throw new IllegalArgumentException("이미 등록된 시리얼번호입니다.");
        }
        Product product = productRepository.save(request.toEntity(createdBy));
        return toResponse(product);
    }

    // 시리얼 번호 부분 일치 검색
    public List<ProductDto.Response> search(ProductDto.SearchRequest request) {
        if (request.serialNumber() == null || request.serialNumber().isBlank()) {
            return List.of();
        }
        return productRepository.findBySerialNumberContaining(request.serialNumber().trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<ProductDto.Response> searchPage(ProductDto.SearchRequest request, Pageable pageable) {
        if (request.serialNumber() == null || request.serialNumber().isBlank()) {
            return Page.empty(pageable);
        }
        return productRepository.findBySerialNumberContaining(request.serialNumber().trim(), pageable)
                .map(this::toResponse);
    }


    // 수리 대상 제품 등록 정보 수정
    @Transactional
    public ProductDto.Response update(Long id, ProductDto.UpdateRequest request, Long updatedBy) {
        findProductModel(request.productModelId());
        if (productRepository.existsBySerialNumberAndIdNot(request.serialNumber(), id)) {
            throw new IllegalArgumentException("이미 등록된 시리얼번호입니다.");
        }
        Product product = findProduct(id);
        product.update(
                request.productModelId(),
                request.customerId(),
                request.serialNumber(),
                request.manufacturedAt(),
                request.purchasedAt(),
                updatedBy
        );
        return toResponse(product);
    }
}
