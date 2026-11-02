package net.likelion.bebc25.fixgo.customer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    @Test
    @DisplayName("모든 필드가 채워진 고객 생성")
    void create_success() {
        // Customer(name, phone, email, zipcode, basicAddress, detailAddress, createdAt, updatedAt, createdBy, updatedBy)
        Customer customer = new Customer(
                "홍길동", "010-1234-5678", "hong@gmail.com",
                "12345", "OO시 OO구 OO동", "OOO동 OOO호", "admin");

        assertThat(customer.getName()).isEqualTo("홍길동");
        assertThat(customer.getPhone()).isEqualTo("010-1234-5678");
        assertThat(customer.getEmail()).isEqualTo("hong@gmail.com");
        assertThat(customer.getZipcode()).isEqualTo("12345");
        assertThat(customer.getBasicAddress()).isEqualTo("OO시 OO구 OO동");
        assertThat(customer.getDetailAddress()).isEqualTo("OOO동 OOO호");
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isEqualTo(customer.getCreatedAt());
        assertThat(customer.getCreatedBy()).isEqualTo("admin");
        assertThat(customer.getUpdatedBy()).isEqualTo("admin");
    }

    @Test
    @DisplayName("not null 조건 체크")
    void create_withoutAddress() {
        Customer customer = new Customer(
                "홍길동", "010-1234-5678", "hong@test.com",
                null, null, null, "admin");

        assertThat(customer.getZipcode()).isNull();
        assertThat(customer.getBasicAddress()).isNull();
        assertThat(customer.getDetailAddress()).isNull();
        assertThat(customer.getName()).isEqualTo("홍길동");
    }
}