package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.part.Part;
import net.likelion.bebc25.fixgo.repair.entity.part.PartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    @Query("""
            SELECT p FROM Part p
            WHERE (:partName IS NULL OR p.name LIKE CONCAT('%', :partName, '%'))
            AND (:status IS NULL OR p.status = :status)
            """)
    Page<Part> searchParts(String partName, PartStatus status, Pageable pageable);

    Optional<Part> findByIdAndStatusNot(Long id, PartStatus status);

    Optional<Part> findByIdAndStatus(Long id, PartStatus status);
}
