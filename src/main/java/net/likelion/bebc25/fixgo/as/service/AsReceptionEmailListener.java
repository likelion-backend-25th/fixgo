package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsReceptionEmailListener {
    private final NotificationService notificationService;

    @Value("${fixgo.url}")
    private String fixgoUrl;

    // A/S 접수 트랜잭션 커밋 후 이메일 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendReceptionEmail(AsReceptionService.ReceptionCreatedEvent event) {

        String portalUrl = fixgoUrl + "/portal/" + event.customerLink();
        // 고객 이메일이 없는 경우 발송하지 않음
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("A/S 접수 완료 이메일 주소 없음. asReceptionId = {}", event.asReceptionId());
            return;
        }

        String title = "[FixGo] A/S 접수 완료 안내";

        String message = """
                안녕하세요. FixGo입니다.
                
                %s 고객님의 A/S 접수가 정상적으로 완료되었습니다.
                
                A/S 접수번호: %s
                접수 상태: 접수 완료
                
                [고객 포털]
                %s
                
                위 링크를 통해 A/S 진행 상황을
                확인하실 수 있습니다.
                
                접수된 제품은 담당자가 확인한 후
                순차적으로 처리할 예정입니다.
                
                감사합니다.
                FixGo 드림
                """.formatted(event.customerName(),
                              event.receptionNumber(),
                              portalUrl);

        EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                event.asReceptionId(),
                event.recipientEmail(),
                title,
                message,
                NotificationType.RECEIVED);
        try {
            EmailDto.Response response =
                    notificationService.sendEmail(request);

            if (response.status() == NotificationStatus.FAILED) {
                log.warn("A/S 접수 완료 이메일 발송 실패. asReceptionId={}, notificationId={}",
                         event.asReceptionId(), response.id());
            }
        } catch (RuntimeException e) {
            log.error("A/S 접수 완료 이메일 발송 오류. asReceptionId={}", event.asReceptionId(), e);
        }

    }
}
