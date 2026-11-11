package net.likelion.bebc25.fixgo.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import net.likelion.bebc25.fixgo.customer.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerDto {

    // 고객 등록
    public record CreateRequest(
            @Schema(description = "고객 이름", example = "홍길동")
            @NotBlank(message = "이름은 필수입력항목입니다.")
            String name,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            @NotBlank(message = "연락처는 필수입력항목입니다.")
            String phone,
            @Schema(description = "고객 이메일", example = "hong@example.com")
            @NotBlank(message = "이메일은 필수입력항목입니다.")
            String email,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
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
            @Schema(description = "고객 이름", example = "홍길동")
            @NotBlank(message = "이름은 필수입력항목입니다.")
            String name,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            @NotBlank(message = "연락처는 필수입력항목입니다.")
            String phone,
            @Schema(description = "고객 이메일", example = "hong@example.com")
            @NotBlank(message = "이메일은 필수입력항목입니다.")
            String email,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
            String detailAddress
    ) {
    }

    // 고객 검색
    public record SearchRequest(
            @Schema(description = "검색할 고객 이름", example = "홍길동")
            String name,
            @Schema(description = "검색할 고객 연락처", example = "010-1234-5678")
            String phone,
            @Schema(description = "검색할 고객 이메일", example = "hong@example.com")
            String email
    ) {
    }

    // 연락처 중복 확인
    public record DuplicateCheckRequest(
            @Schema(description = "중복 확인할 연락처", example = "010-1234-5678")
            String phone
    ) {
    }

    // 중복 확인 응답
    public record DuplicateCheckResponse(
            @Schema(description = "중복 여부", example = "true")
            boolean duplicated
    ) {
    }

    public record Response(
            @Schema(description = "고객 ID", example = "1")
            Long id,
            @Schema(description = "고객 이름", example = "홍길동")
            String name,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String phone,
            @Schema(description = "고객 이메일", example = "hong@example.com")
            String email,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
            String detailAddress,
            @Schema(description = "생성 일시", example = "2026-01-01T10:00:00")
            LocalDateTime createdAt,
            @Schema(description = "수정 일시", example = "2026-01-02T10:00:00")
            LocalDateTime updatedAt,
            @Schema(description = "생성자 ID", example = "1")
            Long createdBy,
            @Schema(description = "수정자 ID", example = "1")
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
            @Schema(description = "엑셀 행 번호", example = "2")
            int rowNumber,
            @Schema(description = "고객 이름", example = "홍길동")
            String name,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String phone,
            @Schema(description = "고객 이메일", example = "hong@example.com")
            String email,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
            String detailAddress,
            @Schema(description = "등록 가능 여부", example = "true")
            boolean registrable,
            @Schema(description = "등록 불가 사유", example = "연락처가 중복되었습니다.")
            String reason
    ) {
    }

    // 엑셀 파일 검증
    public record BulkValidateResponse(
            @Schema(description = "업로드 파일명", example = "customers.xlsx")
            String fileName,
            @Schema(description = "전체 행 수", example = "10")
            int totalCount,
            @Schema(description = "유효 행 수", example = "8")
            int validCount,
            @Schema(description = "무효 행 수", example = "2")
            int invalidCount,
            @Schema(description = "행별 검증 결과 목록", example = "[]")
            List<BulkRowResult> rows
    ) {
    }

    // 등록 대상 data 단건
    public record BulkCreateItem(
            @Schema(description = "엑셀 행 번호", example = "2")
            int rowNumber,
            @Schema(description = "고객 이름", example = "홍길동")
            String name,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String phone,
            @Schema(description = "고객 이메일", example = "hong@example.com")
            String email,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
            String detailAddress
    ) {
    }

    public record BulkCreateRequest(
            @Schema(description = "일괄 등록할 고객 목록", example = "[]")
            List<BulkCreateItem> customers
    ) {
    }

    public record BulkFailure(
            @Schema(description = "실패한 엑셀 행 번호", example = "3")
            int rowNumber,
            @Schema(description = "실패 사유", example = "연락처가 중복되었습니다.")
            String reason
    ) {
    }

    // data 등록 성공/실패 결과값
    public record BulkCreateResponse(
            @Schema(description = "성공 건수", example = "8")
            int successCount,
            @Schema(description = "실패 건수", example = "2")
            int failCount,
            @Schema(description = "실패 목록", example = "[]")
            List<BulkFailure> failures
    ) {
    }
}
