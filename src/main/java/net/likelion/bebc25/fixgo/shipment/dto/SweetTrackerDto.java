package net.likelion.bebc25.fixgo.shipment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SweetTrackerDto {

    // 스마트택배 배송 조회
    public record TrackingRequest(
            // JSON필드 이름을 매핑 apiKey를 t_key랑 매핑하는 것
            @JsonProperty("t_key")
            String apiKey,

            @JsonProperty("t_code")
            String courierCode,

            @JsonProperty("t_invoice")
            String trackingNumber
    ) {}

    // 스마트택배 배송 조회
    // @JsonIgnoreProperties: DTO에 정의하지 않은 불필요한 JSON필드 무시
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrackingResponse(
            String invoiceNo,
            String itemName,
            String result,
            Integer level,
            Boolean complete,
            String completeYN,
            String estimate,
            List<TrackingDetail> trackingDetails,
            Boolean status, // api처리 성공 실패 확인
            String msg  // 오류 메시지
    ) {}

    // 배송 상세
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrackingDetail(
            String code,
            String kind,
            Integer level,
            Long time,
            String timeString,

            @JsonProperty("where")
            String location,

            String remark
    ) {}

    // 스마트택배 API 오류
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ErrorResponse(
            String code,
            String msg,
            Boolean status
    ) {}
}
