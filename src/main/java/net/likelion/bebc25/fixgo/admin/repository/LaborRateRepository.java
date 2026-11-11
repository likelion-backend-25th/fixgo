package net.likelion.bebc25.fixgo.admin.repository;

import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.entity.RateType;
import net.likelion.bebc25.fixgo.product.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LaborRateRepository extends JpaRepository<LaborRate, Long> {

    @Query("""
            SELECT l
            FROM LaborRate l
            WHERE l.productCategory = :category
              AND l.rateType = :rateType
              AND l.effectiveDate <= :date
            ORDER BY l.effectiveDate DESC, l.id DESC
            """)
    List<LaborRate> findLatestRate(@Param("category") Category category, @Param("rateType") RateType rateType, @Param("date") LocalDate date, Pageable pageable);
}