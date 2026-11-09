package net.likelion.bebc25.fixgo.shipment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import net.likelion.bebc25.fixgo.shipment.dto.ShipmentDto;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentShippingService {

    private final ShipmentService shipmentService;
    private final NotificationService notificationService;

    // 배송 시작 및 고객 이메일 알림 발송
    public ShipmentDto.Response startShipping(Long shipmentId) {

        ShipmentService.ShippingEmailData data = shipmentService.getShippingEmailData(shipmentId);

        ShipmentDto.Response response = shipmentService.markShipping(shipmentId);

        String title = "[FixGo] 배송 시작 안내";
        String message = """
                안녕하세요. FixGo입니다.
                
                고객님의 A/S 제품이 발송되었습니다.
                
                A/S 접수번호: %s
                택배사: %s
                운송장번호: %s
                
                해당 택배사 홈페이지에서 배송 조회 서비스를 통해
                배송 진행 상황을 확인하실 수 있습니다.
                
                감사합니다.
                FixGo 드림
                """.formatted(
                data.receptionNumber(),
                data.courier(),
                data.trackingNumber());

        // 이메일 발송 요청 DTO 생성
        EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                data.asReceptionId(),
                data.recipientEmail(),
                title,
                message,
                NotificationType.SHIPPING);

        try {
            EmailDto.Response emailResponse = notificationService.sendEmail(request);

            if (emailResponse.status() == NotificationStatus.FAILED) {
                log.warn("배송 시작 이메일 발송 실패. shipmentId={}, notificationId={}", shipmentId, emailResponse.id());
            }
        } catch (RuntimeException e) {

            log.error("배송 시작 이메일 발송 오류. shipmentId={}", shipmentId, e);
        }

        return response;
    }
}
