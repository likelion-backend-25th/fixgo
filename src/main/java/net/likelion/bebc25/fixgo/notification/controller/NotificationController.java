package net.likelion.bebc25.fixgo.notification.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 이메일 발송
    @PostMapping("/emails")
    public EmailDto.Response sendEmail(@RequestBody EmailDto.CreateRequest request) {
        return notificationService.sendEmail(request);
    }

    // 이메일 발송 내역 조회
    @GetMapping("/emails")
    public Page<EmailDto.Response> getEmailList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return notificationService.getEmailList(keyword, startDate, endDate, pageable);
    }

    // 이메일 발송 상세 조회
    @GetMapping("/emails/{id}")
    public EmailDto.DetailResponse getEmailDetail(@PathVariable Long id) {
        return notificationService.getEmailDetail(id);
    }

    // 이메일 발송 통계 조회
    @GetMapping("/statistics")
    public EmailDto.StatisticsResponse getEmailStatistics() {
        return notificationService.getEmailStatistics();
    }

    // 실패 이메일 재발송
    @PostMapping("/emails/{id}/retry")
    public EmailDto.Response retryEmail(@PathVariable Long id) {
        return notificationService.retryEmail(id);
    }


}
