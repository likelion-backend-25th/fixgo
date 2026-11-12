package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.product.entity.Category;
import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    // 제품군·모델은 호환 관계가 있는 부품만 남긴다. 조건이 없으면 이름과 상태만 본다.
    @Query("""
            SELECT p FROM Part p
            WHERE (:partName IS NULL OR p.name LIKE CONCAT('%', :partName, '%'))
            AND (:status IS NULL OR p.status = :status)
            AND (:productModelId IS NULL OR EXISTS (
                SELECT 1 FROM PartCompatibility modelMatch
                WHERE modelMatch.part = p
                AND modelMatch.productModel.id = :productModelId
            ))
            AND (:category IS NULL OR EXISTS (
                SELECT 1 FROM PartCompatibility categoryMatch
                JOIN categoryMatch.productModel compatibleModel
                WHERE categoryMatch.part = p
                AND compatibleModel.category = :category
            ))
            """)
    Page<Part> searchParts(
            String partName,
            PartStatus status,
            Category category,
            Long productModelId,
            Pageable pageable
    );

    Optional<Part> findByIdAndStatusNot(Long id, PartStatus status);

    Optional<Part> findByIdAndStatus(Long id, PartStatus status);
}
