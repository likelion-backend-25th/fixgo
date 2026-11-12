package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import net.likelion.bebc25.fixgo.portal.service.CustomerPortalLinkService;
import net.likelion.bebc25.fixgo.repair.service.RepairCostService;
import net.likelion.bebc25.fixgo.repair.service.RepairService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsReceptionEmailListener {
    private final NotificationService notificationService;
    private final CustomerPortalLinkService customerPortalLinkService;

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
            EmailDto.Response response = notificationService.sendEmail(request);

            if (response.status() == NotificationStatus.FAILED) {
                log.warn("A/S 접수 완료 이메일 발송 실패. asReceptionId={}, notificationId={}",
                         event.asReceptionId(), response.id());
            }
        } catch (RuntimeException e) {
            log.error("A/S 접수 완료 이메일 발송 오류. asReceptionId={}", event.asReceptionId(), e);
        }

    }

    // 견적 승인 요청 후 이메일 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendEstimateEmail(RepairCostService.EstimateRequestedEvent event) {

        // 고객 이메일이 없는 경우 발송하지 않음
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {
            log.warn("견적 승인 요청 이메일 주소 없음. asReceptionId={}", event.asReceptionId());
            return;
        }
        try {

            String portalUrl = getPortalUrl(event.asReceptionId());

            String title = "[FixGo] A/S 견적서 발급 안내";

            String message = """
                    안녕하세요. FixGo입니다.
                    
                    %s 고객님의 A/S 제품 점검이 완료되어
                    수리 견적서가 발급되었습니다.
                    
                    A/S 접수번호: %s
                    진행 상태: 견적 승인 대기
                    
                    아래 링크를 통해 견적서를 확인하시고
                    수리 진행 여부를 선택해 주세요.
                    
                    [견적서 확인 및 승인]
                    %s
                    
                    고객님의 견적 승인 후
                    수리가 진행될 예정입니다.
                    
                    감사합니다.
                    FixGo 드림
                    """.formatted(
                    event.customerName(),
                    event.receptionNumber(),
                    portalUrl
            );

            EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                    event.asReceptionId(),
                    event.recipientEmail(),
                    title,
                    message,
                    NotificationType.ESTIMATE
            );

            EmailDto.Response response = notificationService.sendEmail(request);

            if (response.status() == NotificationStatus.FAILED) {

                log.warn("견적 승인 요청 이메일 발송 실패. " + "asReceptionId={}, notificationId={}", event.asReceptionId(), response.id());
            }

        } catch (RuntimeException e) {

            log.error("견적 승인 요청 이메일 발송 오류. asReceptionId={}", event.asReceptionId(), e);
        }
    }

    // 수리 완료 후 이메일 발송
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendRepairCompletedEmail(RepairService.RepairCompletedEvent event) {

        // 고객 이메일이 없는 경우 발송하지 않음
        if (event.recipientEmail() == null || event.recipientEmail().isBlank()) {

            log.warn("수리 완료 이메일 주소 없음. asReceptionId={}", event.asReceptionId());

            return;
        }

        try {
            String portalUrl = getPortalUrl(event.asReceptionId());
            String title = "[FixGo] A/S 수리 완료 안내";

            String message;

            // 유상 수리인 경우 결제 안내
            if (event.paymentRequired()) {

                message = """
                        안녕하세요. FixGo입니다.
                        
                        %s 고객님의 A/S 제품 수리가 완료되었습니다.
                        
                        A/S 접수번호: %s
                        진행 상태: 결제 대기
                        
                        아래 링크를 통해 수리 내역과
                        결제 금액을 확인하신 후 결제를 진행해 주세요.
                        
                        [수리 내역 확인 및 결제]
                        %s
                        
                        결제 완료 후 선택하신 수령 방법에 따라
                        제품을 전달해 드릴 예정입니다.
                        
                        감사합니다.
                        FixGo 드림
                        """.formatted(
                        event.customerName(),
                        event.receptionNumber(),
                        portalUrl
                );

            } else {

                // 무상 수리인 경우 수령 안내
                message = """
                        안녕하세요. FixGo입니다.
                        
                        %s 고객님의 A/S 제품 수리가 완료되었습니다.
                        
                        A/S 접수번호: %s
                        수리 구분: 무상 수리
                        
                        아래 링크를 통해 수리 완료 내역과
                        진행 상황을 확인하실 수 있습니다.
                        
                        [고객 포털]
                        %s
                        
                        선택하신 수령 방법에 따라
                        제품을 전달해 드릴 예정입니다.
                        
                        감사합니다.
                        FixGo 드림
                        """.formatted(
                        event.customerName(),
                        event.receptionNumber(),
                        portalUrl
                );
            }

            EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                    event.asReceptionId(),
                    event.recipientEmail(),
                    title,
                    message,
                    NotificationType.REPAIR
            );

            EmailDto.Response response = notificationService.sendEmail(request);

            if (response.status() == NotificationStatus.FAILED) {
                log.warn("수리 완료 이메일 발송 실패. " + "asReceptionId={}, notificationId={}", event.asReceptionId(), response.id());
            }

        } catch (RuntimeException e) {
            log.error("수리 완료 이메일 발송 오류. asReceptionId={}", event.asReceptionId(), e);
        }
    }

    // 기존 고객 포털 링크 생성
    private String getPortalUrl(Long asReceptionId) {

        String token = customerPortalLinkService.getActiveToken(asReceptionId);

        return fixgoUrl.replaceAll("/+$", "") + "/portal/" + token;
    }
}
