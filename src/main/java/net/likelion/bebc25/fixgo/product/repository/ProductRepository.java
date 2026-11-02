package net.likelion.bebc25.fixgo.product.repository;

import net.likelion.bebc25.fixgo.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // 시리얼 번호 등록시 중복 체크
    boolean existsBySerialNumber(String serialNumber);

    // 시리얼 번호로 제품 조회
    Optional<Product> findBySerialNumber(String serialNumber);
}
