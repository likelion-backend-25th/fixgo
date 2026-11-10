package net.likelion.bebc25.fixgo.customer.service;

import net.likelion.bebc25.fixgo.customer.dto.CustomerDto;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.NoSuchElementException;


import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Import(CustomerService.class)
public class CustomerServiceTest {
    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer saveCustomer(String name, String phone, String email) {
        return customerRepository.save(new Customer(name, phone, email, "12345",
                "OO시 OO구 OO대로 123", "OOO동 OOO호", 1L));
    }

    @Test
    @DisplayName("고객 ID로 상세 조회에 성공한다")
    void getById_success() {
        Customer customer = saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");

        CustomerDto.Response response = customerService.getById(customer.getId());

        assertThat(response.id()).isEqualTo(customer.getId());
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.email()).isEqualTo("hong@gmail.com");
        assertThat(response.createdBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("존재하지 않는 고객을 조회하면 예외가 발생한다")
    void getById_fail() {
        assertThatThrownBy(() -> customerService.getById(-1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 고객입니다.");
    }

    @Test
    @DisplayName("이미 등록된 연락처면 중복으로 판정한다")
    void checkDuplicate_true() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");

        boolean duplicated = customerService.checkDuplicate(
                new CustomerDto.DuplicateCheckRequest("010-1234-5678")
        );

        assertThat(duplicated).isTrue();
    }

    @Test
    @DisplayName("미등록 연락처면 중복이 아니다")
    void checkDuplicate_false() {
        boolean duplicated = customerService.checkDuplicate(
                new CustomerDto.DuplicateCheckRequest("010-0000-0000")
        );

        assertThat(duplicated).isFalse();
    }

    @Test
    @DisplayName("고객 등록에 성공하면 저장된 정보를 반환한다")
    void create_success() {
        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest(
                "홍길동", "010-1234-5678", "hong@gmail.com",
                "12345", "OO시 OO구 OO대로 123", "OOO동 OOO호"
        );

        CustomerDto.Response response = customerService.create(request, 1L);

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
        assertThat(response.email()).isEqualTo("hong@gmail.com");
        assertThat(response.createdBy()).isEqualTo(1L);
        assertThat(response.updatedBy()).isEqualTo(1L);
        assertThat(customerRepository.existsByPhone("010-1234-5678")).isTrue();
    }

    @Test
    @DisplayName("이미 등록된 연락처로 고객을 등록하면 예외가 발생한다")
    void create_duplicatePhone() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");

        CustomerDto.CreateRequest request = new CustomerDto.CreateRequest(
                "김철수", "010-1234-5678", "kim@naver.com",
                null, null, null
        );

        assertThatThrownBy(() -> customerService.create(request, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 연락처입니다.");
        assertThat(customerRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("고객명으로 검색한다")
    void searchPage_byName() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");
        saveCustomer("박길동", "010-1234-4444", "park@gmail.com");
        saveCustomer("김철수", "010-1111-2222", "kim@naver.com");

        Page<CustomerDto.Response> page = customerService.searchPage(
                new CustomerDto.SearchRequest("길동", null, null),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(CustomerDto.Response::name)
                .containsExactlyInAnyOrder("홍길동", "박길동");
    }

    @Test
    @DisplayName("연락처로 검색한다")
    void searchPage_byPhone() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");
        saveCustomer("박길동", "010-1234-4444", "park@gmail.com");
        saveCustomer("김철수", "010-1111-2222", "kim@naver.com");

        Page<CustomerDto.Response> page = customerService.searchPage(
                new CustomerDto.SearchRequest(null, "1234", null),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(CustomerDto.Response::phone)
                .containsExactlyInAnyOrder("010-1234-5678", "010-1234-4444");
    }

    @Test
    @DisplayName("이메일로 검색한다")
    void searchPage_byEmail() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");
        saveCustomer("박길동", "010-1234-4444", "park@gmail.com");
        saveCustomer("김철수", "010-1111-2222", "kim@naver.com");

        Page<CustomerDto.Response> page = customerService.searchPage(
                new CustomerDto.SearchRequest(null, null, "gmail"),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(CustomerDto.Response::email)
                .containsExactlyInAnyOrder("hong@gmail.com", "park@gmail.com");
    }

    @Test
    @DisplayName("검색 조건이 없으면 id 내림차순으로 전체 목록을 반환한다")
    void searchPage_noCondition_returnsAllByIdDesc() {
        Customer first = saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");
        Customer second = saveCustomer("박길동", "010-1234-4444", "park@gmail.com");

        Page<CustomerDto.Response> page = customerService.searchPage(
                new CustomerDto.SearchRequest("  ", "  ", "  "),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent()).extracting(CustomerDto.Response::id)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("이름이 있으면 연락처·이메일 조건보다 이름 검색을 우선한다")
    void searchPage_nameTakesPriority() {
        saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");
        saveCustomer("김철수", "010-9999-8888", "kim@gmail.com");

        Page<CustomerDto.Response> page = customerService.searchPage(
                new CustomerDto.SearchRequest("홍길동", "9999", "gmail"),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("고객 정보를 수정한다")
    void update_success() {
        Customer saved = saveCustomer("홍길동", "010-1234-5678", "hong@gmail.com");

        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest(
                "홍길동수정", "010-9999-8888", "hong2@gmail.com",
                "54321", "변경된주소", "변경된상세"
        );

        CustomerDto.Response response = customerService.update(saved.getId(), request, 2L);

        assertThat(response.name()).isEqualTo("홍길동수정");
        assertThat(response.phone()).isEqualTo("010-9999-8888");
        assertThat(response.email()).isEqualTo("hong2@gmail.com");
        assertThat(response.zipcode()).isEqualTo("54321");
        assertThat(response.basicAddress()).isEqualTo("변경된주소");
        assertThat(response.detailAddress()).isEqualTo("변경된상세");
        assertThat(response.updatedBy()).isEqualTo(2L);

        Customer updated = customerRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("홍길동수정");
        assertThat(updated.getUpdatedBy()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 고객을 수정하면 예외가 발생한다")
    void update_notFound() {
        CustomerDto.UpdateRequest request = new CustomerDto.UpdateRequest(
                "이름", "010-0000-0000", "a@b.com",
                null, null, null
        );

        assertThatThrownBy(() -> customerService.update(999L, request, 2L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("존재하지 않는 고객입니다.");
    }
}
