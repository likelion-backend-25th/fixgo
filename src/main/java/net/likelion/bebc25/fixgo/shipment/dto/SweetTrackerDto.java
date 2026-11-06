package net.likelion.bebc25.fixgo.shipment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SweetTrackerDto {

    public record TrackingRequest(
            // 스마트택배 API키
            @JsonProperty("t_key")
            String apikey,

            // 택배사 코드
            @JsonProperty("t_code")
            String courierCode,

            // 운송장 번호
            @JsonProperty("t_invoice")
            String trackingNumber

    ) {}

    // 스마트택배 배송 조회
    public record TrackingResponse<TrackingDetail>(
            String invoiceNo,
            String itemName,
            String result,
            Integer level,
            Boolean complete,
            String completeYN,
            String estimate,
            List<TrackingDetail> trackingDetail
    ) {}

    // 배송 상세 이력
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

    // 스마트택배 API 오류 응답
    public record ErrorResponse(
            String code,
            String msg,
            Boolean status
    ) {}

}

