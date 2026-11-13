package net.likelion.bebc25.fixgo.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Notification API", description = "이메일 발송/내역/통계 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
public class NotificationController {

    private final NotificationService notificationService;

    // 이메일 발송
    @PostMapping("/emails")
    @Operation(summary = "이메일 발송", description = "이메일을 발송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 발송 성공"),
    })
    public EmailDto.Response sendEmail(@RequestBody EmailDto.CreateRequest request) {
        return notificationService.sendEmail(request);
    }

    // 이메일 발송 내역 조회
    @GetMapping("/emails")
    @Operation(summary = "이메일 발송 내역 조회", description = "이메일 발송 내역을 조건으로 페이징 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발송 내역 조회 성공"),
    })
    public Page<EmailDto.Response> getEmailList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) NotificationStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return notificationService.getEmailList(keyword, startDate, endDate, status, pageable);
    }

    // 이메일 발송 상세 조회
    @GetMapping("/emails/{id}")
    @Operation(summary = "이메일 발송 상세 조회", description = "이메일 발송 상세 정보를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발송 상세 조회 성공"),
    })
    public EmailDto.DetailResponse getEmailDetail(@PathVariable Long id) {
        return notificationService.getEmailDetail(id);
    }

    // 이메일 발송 통계 조회
    @GetMapping("/statistics")
    @Operation(summary = "이메일 발송 통계 조회", description = "이메일 발송 통계를 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발송 통계 조회 성공"),
    })
    public EmailDto.StatisticsResponse getEmailStatistics() {
        return notificationService.getEmailStatistics();
    }

    // 실패 이메일 재발송
    @PostMapping("/emails/{id}/retry")
    @Operation(summary = "실패 이메일 재발송", description = "실패한 이메일을 재발송한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발송 성공"),
    })
    public EmailDto.Response retryEmail(@PathVariable Long id) {
        return notificationService.retryEmail(id);
    }


}
