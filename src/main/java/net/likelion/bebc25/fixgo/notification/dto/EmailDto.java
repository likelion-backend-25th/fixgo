package net.likelion.bebc25.fixgo.notification.dto;

import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;

import java.time.LocalDateTime;

public class EmailDto {

    // 이메일 발송 요청
    public record CreateRequest(
            Long asRequestId,
            String recipientEmail,
            String messageTitle,
            String message,
            NotificationType notificationType
    ) {
    }

    public record Response(
            Long id,
            String recipientEmail,
            NotificationType notificationType,
            String messageTitle,
            NotificationStatus status,
            LocalDateTime sentAt
    ) {
        public static Response from(NotificationLog notificationLog) {
            return new Response(
                    notificationLog.getId(),
                    notificationLog.getRecipientEmail(),
                    notificationLog.getNotificationType(),
                    notificationLog.getMessageTitle(),
                    notificationLog.getStatus(),
                    notificationLog.getSentAt()
            );
        }
    }

    public record DetailResponse(
            Long id,
            Long asRequestId,
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

        public static DetailResponse from(NotificationLog notificationLog) {
            return new DetailResponse(
                    notificationLog.getId(),
                    notificationLog.getAsRequest() != null ? notificationLog.getAsRequest().getId() : null,
                    notificationLog.getAsRequest().getCustomer().getName(),
                    notificationLog.getEventKey(),
                    notificationLog.getNotificationType(),
                    notificationLog.getRecipientEmail(),
                    notificationLog.getMessageTitle(),
                    notificationLog.getMessage(),
                    notificationLog.getStatus(),
                    notificationLog.getRetryCount(),
                    notificationLog.getErrorMessage(),
                    notificationLog.getSentAt(),
                    notificationLog.getCreatedAt()
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
