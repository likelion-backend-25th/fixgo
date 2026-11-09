package net.likelion.bebc25.fixgo.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import net.likelion.bebc25.fixgo.common.entity.BaseTimeEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;

    // 고객 이메일 주소
    @Column(nullable = false)
    private String recipientEmail;

    // 이메일 제목
    @Column(nullable = false)
    private String messageTitle;

    // 이메일 본문
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private String errorMessage;

    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_reception_id")
    private AsReception asReception;

    // 이메일 발송 이력
    public static NotificationLog create(
            AsReception asReception,
            Member createdBy,
            String eventKey,
            NotificationType notificationType,
            String recipientEmail,
            String messageTitle,
            String message
    ) {
        NotificationLog notificationLog = new NotificationLog();

        notificationLog.asReception = asReception;
        notificationLog.createdBy = createdBy;
        notificationLog.eventKey = eventKey;
        notificationLog.notificationType = notificationType;
        notificationLog.recipientEmail = recipientEmail;
        notificationLog.messageTitle = messageTitle;
        notificationLog.message = message;

        return notificationLog;

    }

    // 이메일 발송 처리 중
    public void markSending() {
        this.status = NotificationStatus.SENDING;
        this.errorMessage = null;
    }

    // 이메일 발송 성공
    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.errorMessage = null;
        this.sentAt = LocalDateTime.now();
    }

    // 이메일 발송 실패
    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.sentAt = null;
    }

    // 재발송 횟수 추가
    public void increaseRetryCount() {
        this.retryCount++;
    }

}
