package net.likelion.bebc25.fixgo.as.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.shipment.entity.ReturnMethod;

import java.util.List;

public record AsRequestCreateDto(

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
        String requestMemo,

        // null이면 Entity 생성자에서 DELIVERY 적용
        ReturnMethod returnMethod,

        @Valid
        List<AttachmentDto> attachments

) {

    public AsRequestCreateDto {
        attachments = attachments == null
                ? List.of()
                : List.copyOf(attachments);
    }

    public record AttachmentDto(

            @NotBlank
            @Size(max = 500)
            String fileUrl,

            @Size(max = 255)
            String originalName

    ) {
    }

}