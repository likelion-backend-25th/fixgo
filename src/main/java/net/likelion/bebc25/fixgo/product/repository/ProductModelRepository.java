package net.likelion.bebc25.fixgo.product.repository;

import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    // 제품명 중복 확인(등록)
    boolean existsByName(String name);

    // 모델명 중복 확인(등록)
    boolean existsByModelName(String modelName);

    // 제품명 중복 확인(수정)
    boolean existsByNameAndIdNot(String modelName, Long id);

    // 모델명 중복 확인(수정)
    boolean existsByModelNameAndIdNot(String modelName, Long id);

    // keyword 검색 결과 pagination
    @Query("""
            SELECT pm FROM ProductModel pm
            WHERE (:keyword IS NULL OR :keyword = ''
                OR pm.name LIKE CONCAT('%', :keyword, '%')
                OR pm.modelName LIKE CONCAT('%', :keyword, '%'))
            """)
    Page<ProductModel> search(String keyword, Pageable pageable);
}
