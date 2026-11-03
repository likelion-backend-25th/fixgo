package net.likelion.bebc25.fixgo.notification.dto;

import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;

import java.time.LocalDateTime;

public class EmailDto {

    // 이메일 발송 요청
    public record CreateRequest(
            Long asReceptionId,
            String recipientEmail,
            String messageTitle,
            String message,
            NotificationType notificationType
    ) {
    }

    public record Response(
            Long id,
            String customerName,
            String recipientEmail,
            NotificationType notificationType,
            String messageTitle,
            NotificationStatus status,
            LocalDateTime sentAt,
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
            Long id,
            Long asReceptionId,
            String receptionNumber,
            String customerName,
            String eventKey,
            NotificationType notificationType,
            String recipientEmail,
            String messageTitle,
            String message,
            NotificationStatus notificationStatus,
            Integer retryCount,
            String errorMessage,
            LocalDateTime sentAt,
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
            long total,
            long success,
            long failed
    ) {}
}
