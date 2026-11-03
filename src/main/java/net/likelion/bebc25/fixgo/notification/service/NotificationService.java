package net.likelion.bebc25.fixgo.notification.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.client.EmailClient;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.repository.NotificationLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationTransactionService transactionService;
    private final EmailClient emailClient;

    // 이메일 요청 검증
    private void validateRequest(EmailDto.CreateRequest request) {
        if (request == null || request.recipientEmail() == null
                || request.recipientEmail().isBlank()
                || request.messageTitle() == null
                || request.messageTitle().isBlank()
                || request.message() == null
                || request.message().isBlank()
                || request.notificationType() == null) {
            throw new IllegalArgumentException("이메일 발송 필수 정보가 누락 되었습니다.");
        }
    }

    // 이메일 발송
    public EmailDto.Response sendEmail(EmailDto.CreateRequest request) {
        validateRequest(request);

        String eventkey = UUID.randomUUID().toString();
        Long id = transactionService.createNotification(request, eventkey);

        return processEmail(id, false);
    }

    // 이메일 발송 통계 KPI카드
    @Transactional(readOnly = true)
    public EmailDto.StatisticsResponse getEmailStatistics() {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // 토탈 수
        long total = notificationLogRepository.countToday(start, end);

        // 성공 수
        long success = notificationLogRepository.countTodayByStatus(NotificationStatus.SENT, start, end);

        // 실패 수
        long faild = notificationLogRepository.countTodayByStatus(NotificationStatus.FAILED, start, end);

        return new EmailDto.StatisticsResponse(total, success, faild);
    }

    // 이메일 발송 내역 검색 및 페이지네이션
    @Transactional(readOnly = true)
    public Page<EmailDto.Response> getEmailList(String keyword, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        // 날짜 범위
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        // 빈 검색어는 전체 조회
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        // 검색어
        if (keyword != null) {
            keyword = keyword.trim();
        }

        // 시작일
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;

        // 종료일
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        // 이메일 발송 내역 검색 및 페이지네이션
        Page<NotificationLog> logs = notificationLogRepository.searchEmails(keyword, start, end, pageable);

        return logs.map(EmailDto.Response::from);


    }

    // 이메일 발송 상세 조회
    @Transactional(readOnly = true)
    public EmailDto.DetailResponse getEmailDetail(Long id) {
        NotificationLog log = notificationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 발송 내역입니다. id: " + id));

        return EmailDto.DetailResponse.from(log);

    }

    // 실패한 이메일 재발송
    public EmailDto.Response retryEmail(Long id) {
        return processEmail(id, true);
    }

    // 이메일 발송 공통 처리
    private EmailDto.Response processEmail(Long id, boolean retry) {
        NotificationTransactionService.EmailData data = transactionService.markSending(id, retry);

        try {
            emailClient.sendEmail(
                    data.recipientEmail(),
                    data.messageTitle(),
                    data.message()
            );
        } catch (MailException e) {
            String errorMessage = e.getClass().getSimpleName();

            return transactionService.markFailed(id, errorMessage);
        }

        // 이메일 발송 성공
        return transactionService.markSent(id);
    }
}
