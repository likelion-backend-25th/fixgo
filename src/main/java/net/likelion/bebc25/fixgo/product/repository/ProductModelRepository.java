package net.likelion.bebc25.fixgo.product.repository;

import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {
    // 모델명으로 제품 검색
    List<ProductModel> findByModelNameContaining(String keyword);

    // 제품명으로 제품 검색
    List<ProductModel> findByNameContaining(String keyword);
}
