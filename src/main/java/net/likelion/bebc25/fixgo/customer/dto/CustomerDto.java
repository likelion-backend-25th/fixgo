package net.likelion.bebc25.fixgo.customer.dto;

import net.likelion.bebc25.fixgo.customer.entity.Customer;

import java.time.LocalDateTime;

public class CustomerDto {

    // 고객 등록
    public record CreateRequest(
            String name,
            String phone,
            String email,
            String zipcode,
            String basicAddress,
            String detailAddress
    ) {
        public Customer toEntity(Long createdBy) {
            return new Customer(
                    name,
                    phone,
                    email,
                    zipcode,
                    basicAddress,
                    detailAddress,
                    createdBy
            );
        }
    }

    // 고객 정보 수정
    public record UpdateRequest(
            String name,
            String phone,
            String email,
            String zipcode,
            String basicAddress,
            String detailAddress
    ) {
    }

    // 고객 검색
    public record SearchRequest(
            String name,
            String phone,
            String email
    ) {
    }

    // 연락처 중복 확인
    public record DuplicateCheckRequest(
            String phone
    ) {
    }

    public record Response(
            Long id,
            String name,
            String phone,
            String email,
            String zipcode,
            String basicAddress,
            String detailAddress,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            Long createdBy,
            Long updatedBy
    ) {
        public static Response from(Customer customer) {
            return new Response(
                    customer.getId(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getZipcode(),
                    customer.getBasicAddress(),
                    customer.getDetailAddress(),
                    customer.getCreatedAt(),
                    customer.getUpdatedAt(),
                    customer.getCreatedBy(),
                    customer.getUpdatedBy()
            );
        }
    }
}