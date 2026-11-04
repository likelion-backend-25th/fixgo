package net.likelion.bebc25.fixgo.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.notification.entity.EmailPreset;

public class EmailPresetDto {

    // 이메일 프리셋 등록 요청
    public record CreateRequest(
            @NotBlank
            @Size(max = 255)
            String name,

            @NotBlank
            @Size(max = 255)
            String messageTitle,

            @NotBlank
            String message
    ) {}

    // 이메일 프리셋 수정 요청
    public record UpdateRequest(
            @NotBlank
            @Size(max = 255)
            String name,

            @NotBlank
            @Size(max = 255)
            String messageTitle,

            @NotBlank
            String message
    ) {}

    // 이메일 프리셋 조회 응답
    public record Response(
            Long id,
            String name,
            String messageTitle,
            String message
    ) {
        public static Response from(EmailPreset emailPreset) {
            return new Response(
                    emailPreset.getId(),
                    emailPreset.getName(),
                    emailPreset.getMessageTitle(),
                    emailPreset.getMessage()
            );
        }
    }

}
