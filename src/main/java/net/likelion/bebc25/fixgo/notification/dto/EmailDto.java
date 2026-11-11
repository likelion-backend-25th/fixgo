package net.likelion.bebc25.fixgo.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;

import java.time.LocalDateTime;

public class EmailDto {

    // 이메일 발송 요청
    public record CreateRequest(
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @Schema(description = "수신 이메일", example = "customer@example.com")
            String recipientEmail,
            @Schema(description = "메일 제목", example = "수리 완료 안내")
            String messageTitle,
            @Schema(description = "메일 본문", example = "수리가 완료되었습니다.")
            String message,
            @Schema(description = "알림 유형", example = "REPAIR_COMPLETE")
            NotificationType notificationType
    ) {
    }

    public record Response(
            @Schema(description = "발송 로그 ID", example = "1")
            Long id,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "수신 이메일", example = "customer@example.com")
            String recipientEmail,
            @Schema(description = "알림 유형", example = "REPAIR_COMPLETE")
            NotificationType notificationType,
            @Schema(description = "메일 제목", example = "수리 완료 안내")
            String messageTitle,
            @Schema(description = "발송 상태", example = "SUCCESS")
            NotificationStatus status,
            @Schema(description = "발송 시각", example = "2024-01-01T12:00:00")
            LocalDateTime sentAt,
            @Schema(description = "생성 시각", example = "2024-01-01T11:59:00")
            LocalDateTime createdAt
    ) {
        public static Response from(NotificationLog log) {

            // 임시 지울 예정
            String customerName = null;

            if (log.getAsReception() != null && log.getAsReception().getCustomer() != null) {
                customerName = log.getAsReception().getCustomer().getName();
            }
            return new Response(
                    log.getId(),
                    customerName,
                    log.getRecipientEmail(),
                    log.getNotificationType(),
                    log.getMessageTitle(),
                    log.getStatus(),
                    log.getSentAt(),
                    log.getCreatedAt()
            );
        }
    }

    public record DetailResponse(
            @Schema(description = "발송 로그 ID", example = "1")
            Long id,
            @Schema(description = "A/S 접수 ID", example = "1")
            Long asReceptionId,
            @Schema(description = "접수번호", example = "AS-20240101-001")
            String receptionNumber,
            @Schema(description = "고객명", example = "홍길동")
            String customerName,
            @Schema(description = "이벤트 키", example = "repair-complete-1")
            String eventKey,
            @Schema(description = "알림 유형", example = "REPAIR_COMPLETE")
            NotificationType notificationType,
            @Schema(description = "수신 이메일", example = "customer@example.com")
            String recipientEmail,
            @Schema(description = "메일 제목", example = "수리 완료 안내")
            String messageTitle,
            @Schema(description = "메일 본문", example = "수리가 완료되었습니다.")
            String message,
            @Schema(description = "발송 상태", example = "SUCCESS")
            NotificationStatus notificationStatus,
            @Schema(description = "재시도 횟수", example = "0")
            Integer retryCount,
            @Schema(description = "오류 메시지", example = "null")
            String errorMessage,
            @Schema(description = "발송 시각", example = "2024-01-01T12:00:00")
            LocalDateTime sentAt,
            @Schema(description = "생성 시각", example = "2024-01-01T11:59:00")
            LocalDateTime createdAt
    ) {

        public static DetailResponse from(NotificationLog log) {
            return new DetailResponse(
                    log.getId(),
                    log.getAsReception() != null ? log.getAsReception().getId() : null,
                    log.getAsReception() != null ? log.getAsReception().getReceptionNumber() : null,
                    log.getAsReception() != null && log.getAsReception().getCustomer() != null
                            ? log.getAsReception().getCustomer().getName() : null,
                    log.getEventKey(),
                    log.getNotificationType(),
                    log.getRecipientEmail(),
                    log.getMessageTitle(),
                    log.getMessage(),
                    log.getStatus(),
                    log.getRetryCount(),
                    log.getErrorMessage(),
                    log.getSentAt(),
                    log.getCreatedAt()
            );
        }
    }

    // 이메일 총 발송 수 , 성공 수, 실패 수
    public record StatisticsResponse(
            @Schema(description = "총 발송 수", example = "100")
            long total,
            @Schema(description = "성공 수", example = "95")
            long success,
            @Schema(description = "실패 수", example = "5")
            long failed
    ) {}
}
