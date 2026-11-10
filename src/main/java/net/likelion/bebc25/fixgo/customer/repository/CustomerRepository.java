package net.likelion.bebc25.fixgo.customer.repository;

import net.likelion.bebc25.fixgo.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // 고객명으로 고객 검색
    List<Customer> findByNameContaining(String keyword);

    Page<Customer> findByNameContaining(String keyword, Pageable pageable);

    // 연락처로 고객 검색
    List<Customer> findByPhoneContaining(String keyword);

    Page<Customer> findByPhoneContaining(String keyword, Pageable pageable);

    // 이메일로 고객 검색
    List<Customer> findByEmailContaining(String keyword);

    Page<Customer> findByEmailContaining(String keyword, Pageable pageable);

    // 연락처 중복 확인
    boolean existsByPhone(String phone);

    // 엑셀 data와 db의 data 중복 여부 조회
    List<Customer> findByPhoneIn(Collection<String> phones);
}
