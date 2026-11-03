package net.likelion.bebc25.fixgo.notification.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsRequest;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.repository.NotificationLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationTransactionService {
    private final NotificationLogRepository notificationLogRepository;
    private final EntityManager entityManager;

    // 이메일 발송 이력 생성
    @Transactional
    public Long createNotification(EmailDto.CreateRequest request, String eventKey) {

        AsRequest asRequest = null;

        if (request.asRequestId() != null) {
            asRequest = entityManager.find(AsRequest.class, request.asRequestId());
        }
//        if (asRequest == null) {
//            throw new IllegalArgumentException("존재하지 않는 A/S접수입니다. id: " + request.asRequestId());
//        }

        NotificationLog log = NotificationLog.create(
                asRequest,
                null,
                eventKey,
                request.notificationType(),
                request.recipientEmail(),
                request.messageTitle(),
                request.message());


        NotificationLog savedLog = notificationLogRepository.save(log);

        return savedLog.getId();
    }

    // 독립적으로 새로운 트랜잭션에서 실행하게 만들기 위함
    // 발송 처리 중 상태로 변경
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailData markSending(Long id, boolean retry) {
        NotificationLog log = findLogForUpdate(id);

        if (retry) {
            if (log.getStatus() != NotificationStatus.FAILED) {
                throw new IllegalArgumentException("실패한 이메일만 재발송 가능합니다.");
            }
            log.increaseRetryCount();
        } else {
            if (log.getStatus() != NotificationStatus.PENDING) {
                throw new IllegalArgumentException("발송 대기 상태가 아닙니다");
            }
        }

        log.markSending();

        return new EmailData(
                log.getRecipientEmail(),
                log.getMessageTitle(),
                log.getMessage()
        );
    }

    // 이메일 발송 성공 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EmailDto.Response markSent(Long id) {
        NotificationLog log = findLogForUpdate(id);
        if (log.getStatus() != NotificationStatus.SENDING) {
            throw new IllegalArgumentException("발송 처리 중인 이메일이 아닙니다.");
        }
        log.markSent();

        return EmailDto.Response.from(log);

    }

    // 이메일 발송 실패 처리
    @Transactional
    public EmailDto.Response markFailed(Long id, String errorMessage) {
        NotificationLog log = findLogForUpdate(id);

        if (log.getStatus() != NotificationStatus.SENDING) {
            throw new IllegalStateException("발송 처리 중인 이메일이 아닙니다.");
        }

        log.markFailed(errorMessage);
        return EmailDto.Response.from(log);
    }

    // 동일 이메일에 대한 동시 요청 방지
    private NotificationLog findLogForUpdate(Long id) {
        NotificationLog log = entityManager.find(NotificationLog.class, id, LockModeType.PESSIMISTIC_WRITE);

        if (log == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일 발송 내역입니다. id: " + id);
        }

        return log;
    }

    // 발송 정보
    public record EmailData(
            String recipientEmail,
            String messageTitle,
            String message
    ) {}
}
