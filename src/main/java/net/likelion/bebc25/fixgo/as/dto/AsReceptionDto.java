package net.likelion.bebc25.fixgo.as.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.as.entity.AsAttachment;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.customer.entity.Customer;
import net.likelion.bebc25.fixgo.product.entity.Product;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

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
            String Memo,

            // null이면 Entity 생성자에서 DELIVERY 적용
            ReturnMethod returnMethod,

            @Valid
            AsAttachmentReception attachment

    ) {

        public AsReception toEntity(Customer customer, Product product, Member receptionist, Member technician, AsAttachment attachment) {
            return new AsReception("AS-2026", customer, product, receptionist, technician, symptom, symptomDetail, Memo, returnMethod, attachment);
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

    public record Response(

            Long id,

            Customer customer,

            Product product,

            Member technician,

            String symptom,

            String symptomDetail,

            String Memo,

            ReturnMethod returnMethod,

            AsAttachmentReception attachment
    ) {
        public static Response from(AsReception asReception) {
            AsAttachment attachment = asReception.getAttachment();

            return new Response(
                    asReception.getId(),
                    asReception.getCustomer(),
                    asReception.getProduct(),
                    asReception.getTechnician(),
                    asReception.getSymptom(),
                    asReception.getSymptomDetail(),
                    asReception.getMemo(),
                    asReception.getReturnMethod(),
                    attachment == null ? null : new AsAttachmentReception(
                            attachment.getFileUrl(),
                            attachment.getOriginalName())

            );
        }
    }
}