package net.likelion.bebc25.fixgo.notification.service;

import net.likelion.bebc25.fixgo.notification.client.EmailClient;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationLog;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:notification_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.mail.username=test@fixgo.com"
})
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    // 실제 메일은 보내지 않음
    @MockitoBean
    private EmailClient emailClient;

    private EmailDto.CreateRequest createRequest() {
        return new EmailDto.CreateRequest(
                null,
                "test@gmail.com",
                "[FixGo] 접수 완료",
                "A/S 접수가 완료되었습니다.",
                NotificationType.RECEIVED
        );
    }

    @Test
    @DisplayName("이메일 발송 성공 시 SENT 상태가 DB에 저장된다")
    void sendEmail_success_committed() {
        // given
        EmailDto.CreateRequest request = createRequest();

        // when
        EmailDto.Response response =
                notificationService.sendEmail(request);

        // then
        NotificationLog saved = notificationLogRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(saved.getStatus())
                .isEqualTo(NotificationStatus.SENT);

        assertThat(saved.getSentAt()).isNotNull();
        assertThat(saved.getErrorMessage()).isNull();
        assertThat(saved.getRetryCount()).isZero();

        verify(emailClient).sendEmail(
                request.recipientEmail(),
                request.messageTitle(),
                request.message()
        );
    }

    @Test
    @DisplayName("SMTP 발송 실패 시 FAILED 상태가 DB에 저장된다")
    void sendEmail_failure_committed() {
        // given
        EmailDto.CreateRequest request = createRequest();

        doThrow(new MailSendException("SMTP 연결 실패"))
                .when(emailClient)
                .sendEmail(anyString(), anyString(), anyString());

        // when
        EmailDto.Response response =
                notificationService.sendEmail(request);

        // then
        NotificationLog saved = notificationLogRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(saved.getStatus())
                .isEqualTo(NotificationStatus.FAILED);

        assertThat(saved.getErrorMessage())
                .isEqualTo("MailSendException");

        assertThat(saved.getSentAt()).isNull();
        assertThat(saved.getRetryCount()).isZero();
        assertThat(saved.getEventKey()).isNotBlank();
    }

    @Test
    @DisplayName("발송 실패 후 재발송하면 SENT 상태와 재시도 횟수가 DB에 저장된다")
    void retryEmail_success_committed() {
        // given
        EmailDto.CreateRequest request = createRequest();

        doThrow(new MailSendException("SMTP 연결 실패"))
                .doNothing()
                .when(emailClient)
                .sendEmail(anyString(), anyString(), anyString());

        EmailDto.Response failed =
                notificationService.sendEmail(request);

        NotificationLog beforeRetry = notificationLogRepository
                .findById(failed.id())
                .orElseThrow();

        assertThat(beforeRetry.getStatus())
                .isEqualTo(NotificationStatus.FAILED);

        // when
        EmailDto.Response response =
                notificationService.retryEmail(failed.id());

        // then
        NotificationLog saved = notificationLogRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(saved.getId()).isEqualTo(failed.id());
        assertThat(saved.getStatus())
                .isEqualTo(NotificationStatus.SENT);

        assertThat(saved.getRetryCount()).isEqualTo(1);
        assertThat(saved.getSentAt()).isNotNull();
        assertThat(saved.getErrorMessage()).isNull();

        verify(emailClient, times(2))
                .sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("SMTP 재발송 실패 시 FAILED 상태와 재시도 횟수가 DB에 저장된다")
    void retryEmail_failure_committed() {
        // given
        EmailDto.CreateRequest request = createRequest();

        doThrow(new MailSendException("SMTP 연결 실패"))
                .when(emailClient)
                .sendEmail(anyString(), anyString(), anyString());

        EmailDto.Response failed =
                notificationService.sendEmail(request);

        // when
        EmailDto.Response response =
                notificationService.retryEmail(failed.id());

        // then
        NotificationLog saved = notificationLogRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(saved.getStatus())
                .isEqualTo(NotificationStatus.FAILED);

        assertThat(saved.getRetryCount()).isEqualTo(1);
        assertThat(saved.getSentAt()).isNull();

        assertThat(saved.getErrorMessage())
                .isEqualTo("MailSendException");

        verify(emailClient, times(2))
                .sendEmail(anyString(), anyString(), anyString());
    }
}
