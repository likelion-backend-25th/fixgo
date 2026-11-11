package net.likelion.bebc25.fixgo.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import net.likelion.bebc25.fixgo.notification.entity.EmailPreset;

public class EmailPresetDto {

    // 이메일 프리셋 등록 요청
    public record CreateRequest(
            @NotBlank
            @Size(max = 255)
            @Schema(description = "프리셋 이름", example = "수리 완료 안내")
            String name,

            @NotBlank
            @Size(max = 255)
            @Schema(description = "메일 제목", example = "수리가 완료되었습니다")
            String messageTitle,

            @NotBlank
            @Schema(description = "메일 본문", example = "고객님의 제품 수리가 완료되었습니다.")
            String message
    ) {}

    // 이메일 프리셋 수정 요청
    public record UpdateRequest(
            @NotBlank
            @Size(max = 255)
            @Schema(description = "프리셋 이름", example = "수리 완료 안내")
            String name,

            @NotBlank
            @Size(max = 255)
            @Schema(description = "메일 제목", example = "수리가 완료되었습니다")
            String messageTitle,

            @NotBlank
            @Schema(description = "메일 본문", example = "고객님의 제품 수리가 완료되었습니다.")
            String message
    ) {}

    // 이메일 프리셋 조회 응답
    public record Response(
            @Schema(description = "프리셋 ID", example = "1")
            Long id,
            @Schema(description = "프리셋 이름", example = "수리 완료 안내")
            String name,
            @Schema(description = "메일 제목", example = "수리가 완료되었습니다")
            String messageTitle,
            @Schema(description = "메일 본문", example = "고객님의 제품 수리가 완료되었습니다.")
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
