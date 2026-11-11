package net.likelion.bebc25.fixgo.notification.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTransactionServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private NotificationTransactionService transactionService;

    private NotificationLog createLog() {
        NotificationLog log = NotificationLog.create(
                null,
                null,
                "test-event-001",
                NotificationType.RECEIVED,
                "test@gmail.com",
                "[FixGo] 접수 완료",
                "A/S 접수가 완료되었습니다."
        );

        ReflectionTestUtils.setField(log, "id", 1L);

        return log;
    }

    private void mockFindLog(NotificationLog log) {
        when(entityManager.find(
                NotificationLog.class,
                1L,
                LockModeType.PESSIMISTIC_WRITE
        )).thenReturn(log);
    }

    @Test
    @DisplayName("이메일 발송 이력 생성 성공")
    void createNotification_success() {
        // given
        EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                null,
                "test@gmail.com",
                "[FixGo] 접수 완료",
                "A/S 접수가 완료되었습니다.",
                NotificationType.RECEIVED
        );

        when(notificationLogRepository.save(any(NotificationLog.class)))
                .thenAnswer(invocation -> {
                    NotificationLog log = invocation.getArgument(0);
                    ReflectionTestUtils.setField(log, "id", 1L);
                    return log;
                });

        // when
        Long id = transactionService.createNotification(
                request, "test-event-001"
        );

        // then
        assertThat(id).isEqualTo(1L);

        verify(notificationLogRepository).save(argThat(log ->
                                                               log.getStatus() == NotificationStatus.PENDING
                                                                       && log.getEventKey().equals("test-event-001")
                                                                       && log.getRetryCount() == 0
        ));
    }

    @Test
    @DisplayName("발송 시작 시 SENDING 상태로 변경")
    void markSending_success() {
        // given
        NotificationLog log = createLog();
        mockFindLog(log);

        // when
        NotificationTransactionService.EmailData result =
                transactionService.markSending(1L, false);

        // then
        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.SENDING);
        assertThat(log.getRetryCount()).isZero();

        assertThat(result.recipientEmail())
                .isEqualTo("test@gmail.com");
        assertThat(result.messageTitle())
                .isEqualTo("[FixGo] 접수 완료");
        assertThat(result.message())
                .isEqualTo("A/S 접수가 완료되었습니다.");
    }

    @Test
    @DisplayName("발송 성공 시 SENT 상태와 발송일시 저장")
    void markSent_success() {
        // given
        NotificationLog log = createLog();
        log.markSending();
        mockFindLog(log);

        // when
        EmailDto.Response result = transactionService.markSent(1L);

        // then
        assertThat(result.status())
                .isEqualTo(NotificationStatus.SENT);

        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.SENT);
        assertThat(log.getSentAt()).isNotNull();
        assertThat(log.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("발송 실패 시 FAILED 상태와 실패 사유 저장")
    void markFailed_success() {
        // given
        NotificationLog log = createLog();
        log.markSending();
        mockFindLog(log);

        // when
        EmailDto.Response result = transactionService.markFailed(
                1L, "SMTP 연결 실패"
        );

        // then
        assertThat(result.status())
                .isEqualTo(NotificationStatus.FAILED);

        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.FAILED);
        assertThat(log.getErrorMessage())
                .isEqualTo("SMTP 연결 실패");
        assertThat(log.getSentAt()).isNull();
    }

    @Test
    @DisplayName("실패한 이메일 재발송 시 재시도 횟수 증가")
    void markSending_retry_success() {
        // given
        NotificationLog log = createLog();
        log.markSending();
        log.markFailed("SMTP 연결 실패");
        mockFindLog(log);

        // when
        transactionService.markSending(1L, true);

        // then
        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.SENDING);
        assertThat(log.getRetryCount()).isEqualTo(1);
        assertThat(log.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("실패 상태가 아닌 이메일은 재발송 불가")
    void markSending_retry_invalidStatus() {
        // given
        NotificationLog log = createLog();
        mockFindLog(log);

        // when & then
        assertThatThrownBy(() ->
                                   transactionService.markSending(1L, true)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실패한 이메일만");

        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.PENDING);
        assertThat(log.getRetryCount()).isZero();
    }

    @Test
    @DisplayName("발송 대기가 아닌 이메일은 최초 발송 불가")
    void markSending_invalidStatus() {
        // given
        NotificationLog log = createLog();
        log.markSending();
        mockFindLog(log);

        // when & then
        assertThatThrownBy(() ->
                                   transactionService.markSending(1L, false)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("발송 대기 상태");

        assertThat(log.getStatus())
                .isEqualTo(NotificationStatus.SENDING);
    }

    @Test
    @DisplayName("존재하지 않는 이메일은 발송 처리 불가")
    void markSending_notFound() {
        // given
        when(entityManager.find(
                NotificationLog.class,
                1L,
                LockModeType.PESSIMISTIC_WRITE
        )).thenReturn(null);

        // when & then
        assertThatThrownBy(() ->
                                   transactionService.markSending(1L, false)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 이메일");
    }
}
