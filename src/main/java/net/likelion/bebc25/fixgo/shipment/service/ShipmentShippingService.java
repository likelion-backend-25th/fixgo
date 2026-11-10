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

        // 택배사별 배송 조회 링크 생성
        String trackingUrl = getTrackingUrl(data.courierCode());

        ShipmentDto.Response response = shipmentService.markShipping(shipmentId);

        String title = "[FixGo] 배송 시작 안내";
        String message = """
                안녕하세요. FixGo입니다.
                
                고객님의 A/S 제품이 발송되었습니다.
                
                A/S 접수번호: %s
                택배사: %s
                운송장번호: %s
                [배송 조회]
                %s
                
                위 링크에 접속하여 운송장번호를 입력하시면
                배송 진행 상황을 확인하실 수 있습니다.
                
                감사합니다.
                FixGo 드림
                """.formatted(
                data.receptionNumber(),
                data.courier(),
                data.trackingNumber(),
                trackingUrl);

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

    // 택배사별 공식 배송 조회 링크
    private String getTrackingUrl(String courierCode) {
        return switch (courierCode) {
            case "01" -> "https://trace.epost.go.kr/xtts/servlet/kpl.tts.common.svl.SttSVL";
            case "04" -> "https://www.cjlogistics.com/ko/tool/parcel/tracking";
            case "05" -> "https://www.hanjin.com/kor/CMS/DeliveryMgr/WaybillSch.do?mCode=MN029";
            case "06" -> "https://ilogen.com/web/personal/tkSearch";
            case "08" -> "https://www.lotteglogis.com/home/reservation/tracking/index";
            default -> throw new IllegalArgumentException(
                    "지원하지 않는 택배사 코드입니다."
            );
        };
    }
}
