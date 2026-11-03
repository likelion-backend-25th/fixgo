package net.likelion.bebc25.fixgo.notification.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class NotificationLogTest {

    @Autowired
    private EntityManager em;

    private Long notificationLogId;


    // 테스트용 이메일 발송 이력 생성
    @BeforeEach
    void setup() {
        NotificationLog log = NotificationLog.create(
                null,
                null,
                "test-event-001",
                NotificationType.RECEIVED,
                "test@gmail.com",
                "A/S 접수 완료 안내",
                "고객님의 A/S 접수가 완료되었습니다."

        );

        em.persist(log);

        notificationLogId = log.getId();

        em.flush();
        em.clear();

    }

    @Test
    @DisplayName("이메일 발송 이력 생성하면 PENDING 상태이고 재발송 횟수는 0이다.")
    void findNotificationLog() {

        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        // then
        assertThat(log).isNotNull();
        assertThat(log.getId()).isEqualTo(notificationLogId);
        assertThat(log.getEventKey()).isEqualTo("test-event-001");
        assertThat(log.getNotificationType()).isEqualTo(NotificationType.RECEIVED);
        assertThat(log.getRecipientEmail()).isEqualTo("test@gmail.com");
        assertThat(log.getMessageTitle()).isEqualTo("A/S 접수 완료 안내");
        assertThat(log.getMessage()).isEqualTo("고객님의 A/S 접수가 완료되었습니다.");

        assertThat(log.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(log.getRetryCount()).isZero();

        assertThat(log.getAsReception()).isNull();
        assertThat(log.getCreatedBy()).isNull();
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getSentAt()).isNull();
        assertThat(log.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 발송을 시작하면 SENDING 상태로 변경된다.")
    void markSending() {
        // given
        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        // when
        log.markSending();

        em.flush();
        em.clear();

        // then
        NotificationLog savedLog = em.find(NotificationLog.class, notificationLogId);

        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENDING);
        assertThat(savedLog.getErrorMessage()).isNull();

    }

    @Test
    @DisplayName("이메일 발송에 성공하면 SENT 상태가 되고 발송 시간이 기록된다.")
    void markSent() {
        // given
        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        log.markSending();

        // when
        log.markSent();

        em.flush();
        em.clear();

        // then
        NotificationLog savedLog = em.find(NotificationLog.class, notificationLogId);

        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedLog.getSentAt()).isNotNull();
        assertThat(savedLog.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("이메일 발송에 실패하면 FAILED 상태가 되고 오류 메시지가 저장된다.")
    void markFailed() {
        // given
        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        log.markSending();

        // mark
        log.markFailed("SMTP 연결 실패");

        em.flush();
        em.clear();


        // then
        NotificationLog savedLog = em.find(NotificationLog.class, notificationLogId);

        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).isEqualTo("SMTP 연결 실패");
        assertThat(savedLog.getSentAt()).isNull();
    }

    @Test
    @DisplayName("재발송 횟수를 증가시키면 retryCount가 DB에 저장된다.")
    void increaseRetryCount() {
        // given
        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        // when
        log.increaseRetryCount();
        log.increaseRetryCount();

        em.flush();
        em.clear();

        // then
        NotificationLog savedLog = em.find(NotificationLog.class, notificationLogId);

        assertThat(savedLog.getRetryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("이메일 발송 실패 후 재발송에 성공하면 SENT 상태로 변경되고 오류 메시지가 초기화된다.")
    void retry() {
        //given
        NotificationLog log = em.find(NotificationLog.class, notificationLogId);

        log.markSending();
        log.markFailed("SMTP 연결 실패");

        // when
        log.increaseRetryCount();
        log.markSending();
        log.markSent();

        // then
        NotificationLog savedLog = em.find(NotificationLog.class, notificationLogId);

        assertThat(savedLog.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedLog.getRetryCount()).isEqualTo(1);
        assertThat(savedLog.getErrorMessage()).isNull();
        assertThat(savedLog.getSentAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 발송 이력에는 중복되지 않는 eventKey가 저장되어야 한다.")
    void eventKey() {
        NotificationLog duplicateLog = NotificationLog.create(
                null,
                null,
                "test-event-001",
                NotificationType.REPAIR,
                "another@gmail.com",
                "수리 완료 안내",
                "수리가 완료되었습니다."
        );
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> {
            em.persist(duplicateLog);
            em.flush();
        }).isInstanceOf(jakarta.persistence.PersistenceException.class);
    }
}
