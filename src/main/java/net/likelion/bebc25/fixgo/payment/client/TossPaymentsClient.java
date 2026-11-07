package net.likelion.bebc25.fixgo.payment.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class TossPaymentsClient {

    private final RestClient restClient;

    public TossPaymentsClient(@Value("${toss.secret-key}") String secretKey) {
        // 토스 인증 규칙: 시크릿키를 Base64 인코딩
        String encodedKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        // 타임아웃이 없으면 토스가 응답하지 않을 때 무한정 기다린다
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(30));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.tosspayments.com/v1/payments")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .requestFactory(factory)
                .build();
    }

    public TossDto.ConfirmResponse confirm(String paymentKey, String orderId, Long amount) {
        return restClient.post()
                .uri("/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TossDto.ConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .body(TossDto.ConfirmResponse.class);
    }

    public TossDto.CancelResponse cancel(String paymentKey, String cancelReason, TossDto.CancelRequest.RefundReceiveAccount refundReceiveAccount) {
        return restClient.post()
                .uri("/{paymentKey}/cancel", paymentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TossDto.CancelRequest(cancelReason, refundReceiveAccount))
                .retrieve()
                .body(TossDto.CancelResponse.class);
    }

    public TossDto.ConfirmResponse getByOrderId(String orderId) {
        return restClient.get()
                .uri("/orders/{orderId}", orderId)
                .retrieve()
                .body(TossDto.ConfirmResponse.class);
    }
}
