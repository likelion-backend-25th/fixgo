package net.likelion.bebc25.fixgo.customer.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class CustomerRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private CustomerRepository customerRepository;

    private Long customer1Id;
    private Long customer2Id;

    @BeforeEach
    void setUp() {
        Customer customer1 = new Customer(
                "홍길동", "010-1234-5678", "hong@gmail.com",
                "12345", "OO시 OO구 OO대로 123", "OOO동 OOO호", "admin");
        Customer customer2 = new Customer(
                "김철수", "010-1111-2222", "kim@naver.com",
                "12345", "OO시 OO구 OO대로 123", "OOO동 OOO호", "admin");
        Customer customer3 = new Customer(
                "박길동", "010-1234-4444", "park@gmail.com",
                null, null, null, "admin");

        customerRepository.save(customer1);
        customerRepository.save(customer2);
        customerRepository.save(customer3);
        em.flush();
        em.clear();

        customer1Id = customer1.getId();
        customer2Id = customer2.getId();
    }

    @Test
    @DisplayName("고객 등록 및 조회")
    void save_and_findById() {
        Customer found = customerRepository.findById(customer1Id).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("홍길동");
        assertThat(found.getPhone()).isEqualTo("010-1234-5678");
        assertThat(found.getEmail()).isEqualTo("hong@gmail.com");
    }

    @Test
    @DisplayName("고객명 검색")
    void findByNameContaining_multiple() {
        List<Customer> results = customerRepository.findByNameContaining("길동");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Customer::getName)
                .containsExactlyInAnyOrder("홍길동", "박길동");
    }

    @Test
    @DisplayName("존재하지 않는 고객명 검색")
    void findByNameContaining_empty() {
        List<Customer> results = customerRepository.findByNameContaining("수리");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("연락처 검색")
    void findByPhoneContaining() {
        List<Customer> results = customerRepository.findByPhoneContaining("1234");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Customer::getPhone)
                .containsExactlyInAnyOrder("010-1234-5678", "010-1234-4444");
        assertThat(results).extracting(Customer::getId)
                .doesNotContain(customer2Id);
    }

    @Test
    @DisplayName("이메일 검색")
    void findByEmailContaining() {
        List<Customer> results = customerRepository.findByEmailContaining("gmail");

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Customer::getEmail)
                .containsExactlyInAnyOrder("hong@gmail.com", "park@gmail.com");
    }
}