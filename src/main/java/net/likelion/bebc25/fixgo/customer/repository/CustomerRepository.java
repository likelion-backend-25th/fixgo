package net.likelion.bebc25.fixgo.customer.repository;

import net.likelion.bebc25.fixgo.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // 고객명으로 고객 검색
    List<Customer> findByNameContaining(String keyword);

    // 연락처로 고객 검색
    List<Customer> findByPhoneContaining(String keyword);

    // 이메일로 고객 검색
    List<Customer> findByEmailContaining(String keyword);
}
