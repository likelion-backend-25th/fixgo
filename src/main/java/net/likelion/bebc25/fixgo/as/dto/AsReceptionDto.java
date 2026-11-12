package net.likelion.bebc25.fixgo.as.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import net.likelion.bebc25.fixgo.as.entity.AsTechnicianAssignmentHistory;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.repair.dto.repair.RepairResponse;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AsReceptionDto {

    public record UpdateReceptionRequest(
            @Schema(description = "고장 증상", example = "전원 불량")
            @Size(max = 255) String symptom,
            @Schema(description = "고장 증상 상세", example = "전원 버튼을 눌러도 켜지지 않음")
            @Size(max = 255) String symptomDetail,
            @Schema(description = "접수 메모", example = "고객 방문 예정")
            @Size(max = 1000) String memo,
            @Schema(description = "반송 방법", example = "DELIVERY")
            ReturnMethod returnMethod
    ) {
    }

    public record CreateReceptionRequest(

            @Schema(description = "고객 ID", example = "1")
            @NotNull
            Long customerId,

            @Schema(description = "제품 ID", example = "1")
            @NotNull
            Long productId,

            @Schema(description = "담당 기사 ID", example = "3")
            @NotNull
            Long technicianId,

            @Schema(description = "고장 증상", example = "전원 불량")
            @NotBlank
            @Size(max = 255)
            String symptom,

            @Schema(description = "고장 증상 상세", example = "전원 버튼을 눌러도 켜지지 않음")
            @NotBlank
            @Size(max = 255)
            String symptomDetail,

            @Schema(description = "접수 메모", example = "고객 방문 예정")
            @Size(max = 1000)
            String memo,

            // null이면 Entity 생성자에서 DELIVERY 적용
            @Schema(description = "반송 방법 (null이면 DELIVERY)", example = "DELIVERY")
            ReturnMethod returnMethod,

            @Schema(description = "첨부 파일 URL", example = "https://cdn.fixgo.com/files/as-001.jpg")
            String fileUrl

    ) {

        public AsReception toEntity(
                Customer customer,
                Product product,
                Member receptionist,
                Member technician
        ) {
            // DB ID가 발급되기 전까지 사용하는 임시 고유값. 저장 직후 정식 번호로 교체한다.
            String receptionNumber = "PENDING-" + UUID.randomUUID();

            return new AsReception(
                    receptionNumber,
                    customer,
                    product,
                    receptionist,
                    technician,
                    symptom,
                    symptomDetail,
                    memo,
                    returnMethod,
                    fileUrl
            );
        }

    }

    public record ReceptionSearchRequest(
            @Schema(description = "검색 키워드", example = "홍길동")
            String keyword,
            @Schema(description = "접수 상태", example = "RECEIVED")
            AsReceptionStatus status,
            @Schema(description = "담당 기사 ID", example = "3")
            Long technicianId
    ) {}

    public record ListResponse(
            @Schema(description = "접수 ID", example = "1")
            Long id,
            @Schema(description = "접수번호", example = "AS-251111-abc")
            String receptionNumber,
            @Schema(description = "접수 일시", example = "2025-11-11T09:00:00")
            LocalDateTime createdAt,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String customerPhone,
            @Schema(description = "모델명", example = "FixGo Washer X1")
            String modelName,
            @Schema(description = "접수 상태", example = "RECEIVED")
            AsReceptionStatus receptionStatus,
            @Schema(description = "수리 상태", example = "WAITING")
            RepairStatus repairStatus,
            @Schema(description = "담당 기사명", example = "김기사")
            String technicianName
    ) {
        public static ListResponse from(
                AsReception reception,
                Repair repair,
                String modelName
        ) {
            Member technician = reception.getTechnician();

            return new ListResponse(
                    reception.getId(),
                    reception.getReceptionNumber(),
                    reception.getCreatedAt(),
                    reception.getCustomer().getName(),
                    reception.getCustomer().getPhone(),
                    modelName,
                    reception.getStatus(),
                    repair.getStatus(),
                    technician.getName()
            );
        }
    }

    public record MyReceptionDetailResponse(
            @Schema(description = "접수 상세 정보")
            DetailResponse reception,
            @Schema(description = "수리 상세 정보")
            RepairResponse repair
    ) {}

    public record DetailResponse(
            @Schema(description = "접수 ID", example = "1")
            Long id,
            @Schema(description = "접수번호", example = "AS-251111-abc")
            String receptionNumber,
            @Schema(description = "접수 일시", example = "2025-11-11T09:00:00")
            LocalDateTime createdAt,

            @Schema(description = "고객 정보")
            Customer customer,
            @Schema(description = "제품 정보")
            Product product,
            @Schema(description = "모델명", example = "FixGo Washer X1")
            String modelName,
            @Schema(description = "접수원 정보")
            MemberInfo receptionist,
            @Schema(description = "담당 기사 정보")
            MemberInfo technician,

            @Schema(description = "접수 상태", example = "RECEIVED")
            AsReceptionStatus status,
            @Schema(description = "고장 증상", example = "전원 불량")
            String symptom,
            @Schema(description = "고장 증상 상세", example = "전원 버튼을 눌러도 켜지지 않음")
            String symptomDetail,

            @Schema(description = "접수 메모", example = "고객 방문 예정")
            String memo,
            @Schema(description = "반송 방법", example = "DELIVERY")
            ReturnMethod returnMethod,
            @Schema(description = "첨부 파일 URL", example = "https://cdn.fixgo.com/files/as-001.jpg")
            String fileUrl
    ) {
        public static DetailResponse from(AsReception asReception, String modelName) {

            return new DetailResponse(
                    asReception.getId(),
                    asReception.getReceptionNumber(),
                    asReception.getCreatedAt(),

                    asReception.getCustomer(),
                    asReception.getProduct(),
                    modelName,
                    MemberInfo.from(asReception.getReceptionist()),
                    MemberInfo.from(asReception.getTechnician()),

                    asReception.getStatus(),
                    asReception.getSymptom(),
                    asReception.getSymptomDetail(),

                    asReception.getMemo(),
                    asReception.getReturnMethod(),
                    asReception.getFileUrl()
            );
        }
    }

    // 접수증에 필요한 응답
    public record ReceptionReceiptResponse(
            @Schema(description = "접수번호", example = "AS-251111-abc")
            String receptionNumber,
            @Schema(description = "접수 일시", example = "2025-11-11T09:00:00")
            LocalDateTime createdAt,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String customerPhone,
            @Schema(description = "고객 이메일", example = "customer@example.com")
            String customerEmail,
            @Schema(description = "우편번호", example = "06236")
            String zipcode,
            @Schema(description = "기본 주소", example = "서울시 강남구 테헤란로")
            String basicAddress,
            @Schema(description = "상세 주소", example = "101동 202호")
            String detailAddress,
            @Schema(description = "모델명", example = "FixGo Washer X1")
            String modelName,
            @Schema(description = "시리얼 번호", example = "SN-123456")
            String serialNumber,
            @Schema(description = "고장 증상", example = "전원 불량")
            String symptom,
            @Schema(description = "반송 방법", example = "DELIVERY")
            ReturnMethod returnMethod,
            @Schema(description = "접수원명", example = "이접수")
            String receptionistName,
            @Schema(description = "담당 기사명", example = "김기사")
            String technicianName
    ) {
        public static ReceptionReceiptResponse from(
                AsReception reception,
                String modelName
        ) {
            Customer customer = reception.getCustomer();
            Product product = reception.getProduct();
            Member technician = reception.getTechnician();

            return new ReceptionReceiptResponse(
                    reception.getReceptionNumber(),
                    reception.getCreatedAt(),
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getZipcode(),
                    customer.getBasicAddress(),
                    customer.getDetailAddress(),
                    modelName,
                    product.getSerialNumber(),
                    reception.getSymptom(),
                    reception.getReturnMethod(),
                    reception.getReceptionist().getName(),
                    technician == null ? null : technician.getName()
            );
        }
    }

    public record MemberInfo(
            @Schema(description = "회원 ID", example = "1")
            Long id,
            @Schema(description = "회원 이름", example = "김기사")
            String name
    ) {
        public static MemberInfo from(Member member) {
            return member == null ? null : new MemberInfo(member.getId(), member.getName());
        }
    }

    // 등록 응답용 DTO (엔티티 대신 ID 와 필요한 값만 반환, 직원 비밀번호 해시 포함되지 않도록)
    public record CreateReceptionResponse(
            @Schema(description = "접수 ID", example = "1")
            Long id,
            @Schema(description = "접수번호", example = "AS-251111-abc")
            String receptionNumber,
            @Schema(description = "고객 ID", example = "1")
            Long customerId,
            @Schema(description = "제품 ID", example = "1")
            Long productId,
            @Schema(description = "접수원 ID", example = "2")
            Long receptionistId,
            @Schema(description = "담당 기사 ID", example = "3")
            Long technicianId,
            @Schema(description = "접수 상태", example = "RECEIVED")
            AsReceptionStatus status,
            @Schema(description = "접수 일시", example = "2025-11-11T09:00:00")
            LocalDateTime createdAt
    ) {
        public static CreateReceptionResponse from(AsReception reception) {
            return new CreateReceptionResponse(
                    reception.getId(),
                    reception.getReceptionNumber(),
                    reception.getCustomer().getId(),
                    reception.getProduct().getId(),
                    reception.getReceptionist().getId(),
                    reception.getTechnician() == null
                            ? null
                            : reception.getTechnician().getId(),
                    reception.getStatus(),
                    reception.getCreatedAt()
            );
        }
    }

    public record updateTechnicianRequest(
            @Schema(description = "재배정할 기사 ID", example = "5")
            @NotNull Long technicianId,
            @Schema(description = "재배정 사유", example = "담당 기사 일정 변경")
            @NotBlank @Size(max = 1000) String reason
    ) {}

    public record AssignmentHistoryMember(
            @Schema(description = "회원 ID", example = "3")
            Long id,
            @Schema(description = "회원 이름", example = "김기사")
            String name
    ) {}

    public record TechnicianAssignmentHistoryResponse(
            @Schema(description = "접수 ID", example = "1")
            Long receptionId,
            @Schema(description = "이전 담당 기사")
            AssignmentHistoryMember previousTechnician,
            @Schema(description = "신규 담당 기사")
            AssignmentHistoryMember newTechnician,
            @Schema(description = "재배정 사유", example = "담당 기사 일정 변경")
            String reason,
            @Schema(description = "변경 일시", example = "2025-11-11T10:00:00")
            LocalDateTime createdAt,
            @Schema(description = "변경자")
            AssignmentHistoryMember changedBy
    ) {

        public static TechnicianAssignmentHistoryResponse from(
                AsTechnicianAssignmentHistory history
        ) {

            return new TechnicianAssignmentHistoryResponse(
                    history.getAsReception().getId(),
                    toMember(history.getPreviousTechnician()),
                    toMember(history.getNewTechnician()),
                    history.getReason(),
                    history.getCreatedAt(),
                    toMember(history.getChangedBy())
            );
        }

        private static AssignmentHistoryMember toMember(Member member) {
            if (member == null) {
                return null;
            }
            return new AssignmentHistoryMember(member.getId(), member.getName());
        }

    }

    /**
     * 고객 포탈 접수 영역.
     */
    public record CustomerReceptionView(
            @Schema(description = "접수번호", example = "AS-251111-abc")
            String receptionNumber,
            @Schema(description = "접수 일시", example = "2025-11-11T09:00:00")
            LocalDateTime receivedAt,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "고객 연락처", example = "010-1234-5678")
            String customerPhone,
            @Schema(description = "모델명", example = "FixGo Washer X1")
            String modelName,
            @Schema(description = "시리얼 번호", example = "SN-123456")
            String serialNumber,
            @Schema(description = "고장 증상", example = "전원 불량")
            String symptom,
            @Schema(description = "고장 증상 상세", example = "전원 버튼을 눌러도 켜지지 않음")
            String symptomDetail,
            @Schema(description = "접수 상태", example = "RECEIVED")
            AsReceptionStatus status,
            @Schema(description = "접수 상태 변경 이력")
            List<StatusHistoryItem> histories
    ) {
        public static CustomerReceptionView of(
                AsReception reception,
                String modelName,
                List<StatusHistoryItem> histories
        ) {
            Customer customer = reception.getCustomer();

            return new CustomerReceptionView(
                    reception.getReceptionNumber(),
                    reception.getCreatedAt(),
                    customer.getName(),
                    customer.getPhone(),
                    modelName,
                    reception.getProduct().getSerialNumber(),
                    reception.getSymptom(),
                    reception.getSymptomDetail(),
                    reception.getStatus(),
                    histories
            );
        }

        public record StatusHistoryItem(
                @Schema(description = "변경된 접수 상태", example = "RECEIVED")
                AsReceptionStatus status,
                @Schema(description = "변경 일시", example = "2025-11-11T09:00:00")
                LocalDateTime changedAt
        ) {
            /**
             * 접수 상태 전환 행만 고객에게 보여 준다.
             * 같은 테이블에 수리 상태 행(예: REPAIRING → COMPLETED)도 쌓이는데, 수리 행의 from은 항상 접수 상태가 아니므로
             * from(최초 접수는 null)과 to가 모두 접수 상태인 행만 고른다. to만 보면 수리 COMPLETED가 접수 완료로 보인다.
             */
            public static Optional<StatusHistoryItem> from(AsStatusHistory history) {
                boolean fromIsReception = history.getFromStatus() == null
                        || findReceptionStatus(history.getFromStatus()).isPresent();
                if (!fromIsReception) {
                    return Optional.empty();
                }
                return findReceptionStatus(history.getToStatus())
                        .map(status -> new StatusHistoryItem(status, history.getCreatedAt()));
            }

            private static Optional<AsReceptionStatus> findReceptionStatus(String name) {
                // 없어진 보증 판정 이력은 점검으로 본다.
                if ("WARRANTY_REVIEW".equals(name)) {
                    return Optional.of(AsReceptionStatus.INSPECTION);
                }
                return Arrays.stream(AsReceptionStatus.values())
                        .filter(status -> status.name().equals(name))
                        .findFirst();
            }
        }
    }
}
