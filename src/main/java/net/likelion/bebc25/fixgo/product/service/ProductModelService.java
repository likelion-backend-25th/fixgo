package net.likelion.bebc25.fixgo.product.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.product.dto.ProductModelDto;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import net.likelion.bebc25.fixgo.product.repository.ProductModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
        // name/modelname duplication check?
//        if (ProductModelDto.existsByName(request.name()) || ProductModelDto.existsByModelName(request.modelName())) {
//            throw new IllegalArgumentException("이미 등록된 제품명/모델명입니다.");
//        }
        ProductModel productModel = productModelRepository.save(request.toEntity(createdBy));
        return ProductModelDto.Response.from(productModel);
    }

    public List<ProductModelDto.Response> search(ProductModelDto.SearchRequest request) {
        // 제품명 검색
        if (request.name() != null && !request.name().isBlank()) {
            return productModelRepository.findByNameContaining(request.name())
                    .stream()
                    .map(ProductModelDto.Response::from)
                    .toList();
        }
        // 모델명 검색
        else if (request.modelName() != null && !request.modelName().isBlank()) {
            return productModelRepository.findByModelNameContaining(request.modelName())
                    .stream()
                    .map(ProductModelDto.Response::from)
                    .toList();
        } else {
            return List.of();
        }
    }

    // 제품 정보 수정
    @Transactional
    public ProductModelDto.Response update(Long id, ProductModelDto.UpdateRequest request, Long updatedBy) {
        ProductModel productModel = findProductModel(id);
        productModel.update(
                request.name(), request.modelName(), request.category(),
                request.warrantyMonths(), request.isActive(), updatedBy
        );
        return ProductModelDto.Response.from(productModel);
    }
}
