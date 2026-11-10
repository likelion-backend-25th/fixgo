package net.likelion.bebc25.fixgo.customer.dto;

import jakarta.validation.constraints.NotBlank;
import net.likelion.bebc25.fixgo.customer.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerDto {

    // 고객 등록
    public record CreateRequest(
            @NotBlank(message = "이름은 필수입력항목입니다.")
            String name,
            @NotBlank(message = "연락처는 필수입력항목입니다.")
            String phone,
            @NotBlank(message = "이메일은 필수입력항목입니다.")
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
            @NotBlank(message = "이름은 필수입력항목입니다.")
            String name,
            @NotBlank(message = "연락처는 필수입력항목입니다.")
            String phone,
            @NotBlank(message = "이메일은 필수입력항목입니다.")
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

    // 중복 확인 응답
    public record DuplicateCheckResponse(
            boolean duplicated
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

    // 엑셀 파일 한 행 data
    public record BulkRowResult(
            int rowNumber,
            String name,
            String phone,
            String email,
            String zipcode,
            String basicAddress,
            String detailAddress,
            boolean registrable,
            String reason
    ) {
    }

    // 엑셀 파일 검증
    public record BulkValidateResponse(
            String fileName,
            int totalCount,
            int validCount,
            int invalidCount,
            List<BulkRowResult> rows
    ) {
    }

    // 등록 대상 data 단건
    public record BulkCreateItem(
            int rowNumber,
            String name,
            String phone,
            String email,
            String zipcode,
            String basicAddress,
            String detailAddress
    ) {
    }

    public record BulkCreateRequest(
            List<BulkCreateItem> customers
    ) {
    }

    public record BulkFailure(
            int rowNumber,
            String reason
    ) {
    }

    // data 등록 성공/실패 결과값
    public record BulkCreateResponse(
            int successCount,
            int failCount,
            List<BulkFailure> failures
    ) {
    }
}