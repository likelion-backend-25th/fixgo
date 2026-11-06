package net.likelion.bebc25.fixgo.notification.service;

import net.likelion.bebc25.fixgo.notification.client.EmailClient;
import net.likelion.bebc25.fixgo.notification.dto.EmailDto;
import net.likelion.bebc25.fixgo.notification.entity.NotificationStatus;
import net.likelion.bebc25.fixgo.notification.entity.NotificationType;
import net.likelion.bebc25.fixgo.notification.repository.NotificationLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationTransactionService transactionService;

    @Mock
    private EmailClient emailClient;

    @InjectMocks
    private NotificationService notificationService;

    private EmailDto.CreateRequest createRequest() {
        return new EmailDto.CreateRequest(
                null,
                "test@gmail.com",
                "[FixGo] A/S 접수 완료",
                "A/S 접수가 완료되었습니다.",
                NotificationType.RECEIVED
        );
    }

    private EmailDto.Response createResponse(
            Long id,
            NotificationStatus status
    ) {
        return new EmailDto.Response(
                id,
                null,
                "test@gmail.com",
                NotificationType.RECEIVED,
                "[FixGo] A/S 접수 완료",
                status,
                null,
                null
        );
    }

    private NotificationTransactionService.EmailData createEmailData() {
        return new NotificationTransactionService.EmailData(
                "test@gmail.com",
                "[FixGo] A/S 접수 완료",
                "A/S 접수가 완료되었습니다."
        );
    }

    @Test
    @DisplayName("이메일 발송 성공")
    void sendEmail_success() {
        // given
        EmailDto.CreateRequest request = createRequest();
        EmailDto.Response expected = createResponse(
                1L, NotificationStatus.SENT
        );

        when(transactionService.createNotification(
                eq(request), anyString()
        )).thenReturn(1L);

        when(transactionService.markSending(1L, false))
                .thenReturn(createEmailData());

        when(transactionService.markSent(1L))
                .thenReturn(expected);

        // when
        EmailDto.Response result = notificationService.sendEmail(request);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);

        InOrder inOrder = inOrder(transactionService, emailClient);

        inOrder.verify(transactionService).createNotification(
                eq(request), anyString()
        );
        inOrder.verify(transactionService).markSending(1L, false);
        inOrder.verify(emailClient).sendEmail(
                "test@gmail.com",
                "[FixGo] A/S 접수 완료",
                "A/S 접수가 완료되었습니다."
        );
        inOrder.verify(transactionService).markSent(1L);

        verify(transactionService, never())
                .markFailed(anyLong(), anyString());
    }

    @Test
    @DisplayName("이메일 발송 실패 시 FAILED 처리")
    void sendEmail_failure() {
        // given
        EmailDto.CreateRequest request = createRequest();
        EmailDto.Response expected = createResponse(
                1L, NotificationStatus.FAILED
        );

        when(transactionService.createNotification(
                eq(request), anyString()
        )).thenReturn(1L);

        when(transactionService.markSending(1L, false))
                .thenReturn(createEmailData());

        doThrow(new MailSendException("SMTP 발송 실패"))
                .when(emailClient)
                .sendEmail(anyString(), anyString(), anyString());

        when(transactionService.markFailed(
                1L, "MailSendException"
        )).thenReturn(expected);

        // when
        EmailDto.Response result = notificationService.sendEmail(request);

        // then
        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);

        verify(transactionService).markFailed(
                1L, "MailSendException"
        );
        verify(transactionService, never()).markSent(anyLong());
    }

    @Test
    @DisplayName("이메일 필수 정보 누락 시 발송 실패")
    void sendEmail_invalidRequest() {
        // given
        EmailDto.CreateRequest request = new EmailDto.CreateRequest(
                null,
                "",
                "[FixGo] 안내",
                "안내 내용",
                NotificationType.RECEIVED
        );

        // when & then
        assertThatThrownBy(() -> notificationService.sendEmail(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수 정보");

        verifyNoInteractions(
                transactionService,
                emailClient,
                notificationLogRepository
        );
    }

    @Test
    @DisplayName("실패한 이메일 재발송 성공")
    void retryEmail_success() {
        // given
        Long id = 1L;
        EmailDto.Response expected = createResponse(
                id, NotificationStatus.SENT
        );

        when(transactionService.markSending(id, true))
                .thenReturn(createEmailData());

        when(transactionService.markSent(id))
                .thenReturn(expected);

        // when
        EmailDto.Response result = notificationService.retryEmail(id);

        // then
        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);

        InOrder inOrder = inOrder(transactionService, emailClient);

        inOrder.verify(transactionService).markSending(id, true);
        inOrder.verify(emailClient).sendEmail(
                "test@gmail.com",
                "[FixGo] A/S 접수 완료",
                "A/S 접수가 완료되었습니다."
        );
        inOrder.verify(transactionService).markSent(id);

        verify(transactionService, never())
                .createNotification(any(), anyString());
    }

    @Test
    @DisplayName("재발송 실패 시 FAILED 처리")
    void retryEmail_failure() {
        // given
        Long id = 1L;
        EmailDto.Response expected = createResponse(
                id, NotificationStatus.FAILED
        );

        when(transactionService.markSending(id, true))
                .thenReturn(createEmailData());

        doThrow(new MailSendException("SMTP 재발송 실패"))
                .when(emailClient)
                .sendEmail(anyString(), anyString(), anyString());

        when(transactionService.markFailed(
                id, "MailSendException"
        )).thenReturn(expected);

        // when
        EmailDto.Response result = notificationService.retryEmail(id);

        // then
        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);

        verify(transactionService).markFailed(
                id, "MailSendException"
        );
        verify(transactionService, never()).markSent(anyLong());
    }

    @Test
    @DisplayName("재발송 불가능한 상태에서는 이메일을 발송하지 않음")
    void retryEmail_invalidStatus() {
        // given
        Long id = 1L;

        when(transactionService.markSending(id, true))
                .thenThrow(new IllegalArgumentException(
                        "실패한 이메일만 재발송 가능합니다."
                ));

        // when & then
        assertThatThrownBy(() -> notificationService.retryEmail(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("실패한 이메일만");

        verifyNoInteractions(emailClient);
        verify(transactionService, never()).markSent(anyLong());
        verify(transactionService, never())
                .markFailed(anyLong(), anyString());
    }
}
