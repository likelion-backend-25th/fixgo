package net.likelion.bebc25.fixgo.product.repository;

import net.likelion.bebc25.fixgo.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 시리얼 번호 등록시 중복 체크
    boolean existsBySerialNumber(String serialNumber);

    // 시리얼 번호 수정시 중복 체크
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    // 시리얼 번호 부분 일치 검색
    List<Product> findBySerialNumberContaining(String serialNumber);

    Page<Product> findBySerialNumberContaining(String serialNumber, Pageable pageable);

    // 고객 보유 제품 조회
    List<Product> findByCustomerId(Long customerId);
}
