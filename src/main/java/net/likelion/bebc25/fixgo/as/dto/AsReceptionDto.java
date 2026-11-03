package net.likelion.bebc25.fixgo.as.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AsReceptionDto {

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
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMss")),
                    UUID.randomUUID()
            );

            return new AsReception(receptionNumber, customer, product, receptionist, technician, symptom, symptomDetail, memo, returnMethod, fileUrl);
        }

    }

    public record AsAttachmentReception(

            @NotBlank
            @Size(max = 500)
            String fileUrl,

            @Size(max = 255)
            String originalName

    ) {
    }

    public record ListResponse(
            Long id,
            String receptionNumber,
            LocalDateTime createdAt,
            String customerName,
            String customerPhone,
            String modelName,
            AsReceptionStatus status,
            String technicianName
    ) {
        public static ListResponse from(
                AsReception reception,
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
                    technician == null ? null : technician.getName()
            );
        }
    }

    public record DetailResponse(
            Long id,
            String receptionNumber,
            LocalDateTime createdAt,

            Customer customer,
            Product product,
            Member receptionist,
            Member technician,

            AsReceptionStatus status,
            LocalDateTime statusChangedAt,
            LocalDateTime closedAt,

            String symptom,
            String symptomDetail,
            String memo,

            ReturnMethod returnMethod,
            String fileUrl
    ) {
        public static DetailResponse from(AsReception asReception) {

            return new DetailResponse(
                    asReception.getId(),
                    asReception.getReceptionNumber(),
                    asReception.getCreatedAt(),

                    asReception.getCustomer(),
                    asReception.getProduct(),
                    asReception.getReceptionist(),
                    asReception.getTechnician(),

                    asReception.getStatus(),
                    asReception.getStatusChangedAt(),
                    asReception.getClosedAt(),

                    asReception.getSymptom(),
                    asReception.getSymptomDetail(),
                    asReception.getMemo(),
                    asReception.getReturnMethod(),
                    asReception.getFileUrl()
            );
        }
    }
}