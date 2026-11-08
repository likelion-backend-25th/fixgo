package net.likelion.bebc25.fixgo.shipment.client;

import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.fixgo.shipment.dto.SweetTrackerDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class SweetTrackerClient {

    private final RestClient restClient;
    private final String apiKey;

    public SweetTrackerClient(
            RestClient.Builder restClientBuilder,
            @Value("${sweettracker.base-url}") String baseUrl,
            @Value("${sweettracker.api-key}") String apiKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();

        this.apiKey = apiKey;
    }

    // 운송장 배송 조회
    public SweetTrackerDto.TrackingResponse getTrackingInfo(
            String courierCode,
            String trackingNumber
    ) {
        SweetTrackerDto.TrackingRequest request = new SweetTrackerDto.TrackingRequest(
                apiKey,
                courierCode,
                trackingNumber);

        try {
            SweetTrackerDto.TrackingResponse response = restClient.post()
                    .uri("/api/v1/trackingInfo")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SweetTrackerDto.TrackingResponse.class);

            // 응답 없음
            if (response == null) {
                throw new IllegalStateException("배송 조회 응답이 없습니다.");
            }

            // API에서 실패 응답을 반환한 경우
            if (Boolean.FALSE.equals(response.status())) {
                throw new IllegalStateException("배송 조회 실패: " + response.msg());
            }

            // 배송 정보 누락
            if (response.invoiceNo() == null || response.invoiceNo().isBlank()) {
                throw new IllegalStateException("배송 조회 결과에 운송장번호가 없습니다.");
            }

            // 요청한 운송장번호와 응답이 다른 경우
            if (!trackingNumber.equals(response.invoiceNo())) {
                throw new IllegalStateException("요청한 운송장번호와 조회 결과가 일치하지 않습니다.");
            }

            return response;

        } catch (RestClientException e) {
            log.error("SweetTracker API 호출 실패. courierCode={}", courierCode, e);

            throw new IllegalStateException("배송 조회 서비스 호출에 실패했습니다.", e);
        }
    }
}
