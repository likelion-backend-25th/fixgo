package net.likelion.bebc25.fixgo.product.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.product.dto.ProductModelDto;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductModelService {

    private final ProductModelRepository productModelRepository;

    // id로 product model entity 조회(내부용)
    private ProductModel findProductModel(Long id) {
        return productModelRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("존재하지 않는 모델입니다."));
    }

    // product model 상세조회(Controller, Dto 반환용)
    public ProductModelDto.Response getById(Long id) {
        return ProductModelDto.Response.from(findProductModel(id));
    }

    // 제품 등록
    @Transactional
    public ProductModelDto.Response create(ProductModelDto.CreateRequest request, Long createdBy) {
        // 제품명/모델명 중복 체크
        if (productModelRepository.existsByName((request.name()))) {
            throw new IllegalArgumentException("이미 등록된 제품명입니다.");
        } else if (productModelRepository.existsByModelName((request.modelName()))) {
            throw new IllegalArgumentException("이미 등록된 모델명입니다.");
        }
        ProductModel productModel = productModelRepository.save(request.toEntity(createdBy));
        return ProductModelDto.Response.from(productModel);
    }

    // 검색 결과 및 pagination
    public Page<ProductModelDto.Response> search(String keyword, Pageable pageable) {
        return productModelRepository.search(keyword, pageable)
                .map(ProductModelDto.Response::from);
    }

    // 제품 정보 수정
    @Transactional
    public ProductModelDto.Response update(Long id, ProductModelDto.UpdateRequest request, Long updatedBy) {
        // 제품명/모델명 중복 체크
        if (productModelRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new IllegalArgumentException("이미 등록된 제품명입니다.");
        } else if (productModelRepository.existsByModelNameAndIdNot(request.modelName(), id)) {
            throw new IllegalArgumentException("이미 등록된 모델명입니다.");
        }
        ProductModel productModel = findProductModel(id);
        productModel.update(
                request.name(), request.modelName(), request.category(),
                request.warrantyMonths(), request.isActive(), updatedBy
        );
        return ProductModelDto.Response.from(productModel);
    }
}
