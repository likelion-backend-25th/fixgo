package net.likelion.bebc25.fixgo.as.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsTechnicianAssignmentHistory;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AsReceptionDto {

    public record UpdateReceptionRequest(
            @Size(max = 255) String symptom,
            @Size(max = 255) String symptomDetail,
            @Size(max = 1000) String memo,
            ReturnMethod returnMethod
    ) {
    }

    public record CreateReceptionRequest(

            @NotNull
            Long customerId,

            @NotNull
            Long productId,

            @NotNull
            Long technicianId,

            @NotBlank
            @Size(max = 255)
            String symptom,

            @NotBlank
            @Size(max = 255)
            String symptomDetail,

            @Size(max = 1000)
            String memo,

            // null이면 Entity 생성자에서 DELIVERY 적용
            ReturnMethod returnMethod,

            String fileUrl

    ) {

        public AsReception toEntity(
                Customer customer,
                Product product,
                Member receptionist,
                Member technician
        ) {
            String receptionNumber = "AS-%s-%s".formatted(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")),
                    UUID.randomUUID()
            );

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
            String keyword,
            AsReceptionStatus status,
            Long technicianId
    ) {}

    public record ListResponse(
            Long id,
            String receptionNumber,
            LocalDateTime createdAt,
            String customerName,
            String customerPhone,
            String modelName,
            AsReceptionStatus receptionStatus,
            RepairStatus repairStatus,
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

    public record DetailResponse(
            Long id,
            String receptionNumber,
            LocalDateTime createdAt,

            Customer customer,
            Product product,
            String modelName,
            MemberInfo receptionist,
            MemberInfo technician,

            AsReceptionStatus status,
            String symptom,
            String symptomDetail,

            String memo,
            ReturnMethod returnMethod,
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
            String receptionNumber,
            LocalDateTime createdAt,
            String customerName,
            String customerPhone,
            String customerEmail,
            String zipcode,
            String basicAddress,
            String detailAddress,
            String modelName,
            String serialNumber,
            String symptom,
            ReturnMethod returnMethod,
            String receptionistName,
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

    public record MemberInfo(Long id, String name) {
        public static MemberInfo from(Member member) {
            return member == null ? null : new MemberInfo(member.getId(), member.getName());
        }
    }

    // 등록 응답용 DTO (엔티티 대신 ID 와 필요한 값만 반환, 직원 비밀번호 해시 포함되지 않도록)
    public record CreateReceptionResponse(
            Long id,
            String receptionNumber,
            Long customerId,
            Long productId,
            Long receptionistId,
            Long technicianId,
            AsReceptionStatus status,
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
            @NotNull Long technicianId,
            @NotBlank @Size(max = 1000) String reason
    ) {}

    public record AssignmentHistoryMember(Long id, String name) {}

    public record TechnicianAssignmentHistoryResponse(
            Long receptionId,
            AssignmentHistoryMember previousTechnician,
            AssignmentHistoryMember newTechnician,
            String reason,
            LocalDateTime createdAt,
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

}
