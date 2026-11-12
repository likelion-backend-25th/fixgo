package net.likelion.bebc25.fixgo.product.repository;

import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithReadLock(@Param("id") Long id);

    // 시리얼 번호 등록시 중복 체크
    boolean existsBySerialNumber(String serialNumber);

    // 시리얼 번호 수정시 중복 체크
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    // 시리얼 번호 부분 일치 검색
    List<Product> findBySerialNumberContaining(String serialNumber);

    Page<Product> findBySerialNumberContaining(String serialNumber, Pageable pageable);

}
