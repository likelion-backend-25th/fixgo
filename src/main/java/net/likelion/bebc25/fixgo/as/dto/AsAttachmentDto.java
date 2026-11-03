package net.likelion.bebc25.fixgo.as.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AsAttachmentDto {

    public record Response(
            @NotBlank
            @Size(max = 500)
            String fileUrl,

            @Size(max = 255)
            String originalName
    ) {}

}
