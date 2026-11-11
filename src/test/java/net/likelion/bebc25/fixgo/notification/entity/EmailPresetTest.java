package net.likelion.bebc25.fixgo.notification.entity;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.config.JpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
public class EmailPresetTest {

    @Autowired
    private EntityManager em;

    private Long emailPresetId;

    @BeforeEach
    void setup() {
        // 이메일 프리셋 생성
        EmailPreset preset = EmailPreset.create(
                "접수 완료 안내",
                "[FixGo] A/S 접수 완료 안내",
                "고객님의 A/S 접수가 정상적으로 완료되었습니다."
        );

        em.persist(preset);

        emailPresetId = preset.getId();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("이메일 프리셋을 등록하면 이름, 제목, 본문이 저장된다.")
    void create() {
        // when
        EmailPreset preset = em.find(EmailPreset.class, emailPresetId);

        // then
        assertThat(preset).isNotNull();
        assertThat(preset.getId()).isEqualTo(emailPresetId);
        assertThat(preset.getName()).isEqualTo("접수 완료 안내");
        assertThat(preset.getMessageTitle()).isEqualTo("[FixGo] A/S 접수 완료 안내");
        assertThat(preset.getMessage()).isEqualTo("고객님의 A/S 접수가 정상적으로 완료되었습니다.");
        assertThat(preset.getCreatedAt()).isNotNull();
        assertThat(preset.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("이메일 프리셋을 수정하면 이름, 제목, 본문이 변경된다.")
    void update() {
        // given
        EmailPreset preset = em.find(EmailPreset.class, emailPresetId);

        // when
        preset.update(
                "수리 완료 안내",
                "[FixGo] 수리 완료 안내",
                "고객님의 제품 수리가 완료되었습니다."
        );

        em.flush();
        em.clear();

        // then
        EmailPreset savedPreset = em.find(EmailPreset.class, emailPresetId);

        assertThat(savedPreset.getName()).isEqualTo("수리 완료 안내");
        assertThat(savedPreset.getMessageTitle()).isEqualTo("[FixGo] 수리 완료 안내");
        assertThat(savedPreset.getMessage()).isEqualTo("고객님의 제품 수리가 완료되었습니다.");
    }

    @Test
    @DisplayName("이메일 프리셋을 수정해도 생성일시는 변경되지 않는다.")
    void updateCreatedAt() {
        // given
        EmailPreset preset = em.find(EmailPreset.class, emailPresetId);
        LocalDateTime originalCreatedAt = preset.getCreatedAt();

        // when
        preset.update(
                "견적서 발급 안내",
                "[FixGo] 견적서 발급 안내",
                "수리 견적서가 발급되었습니다."
        );

        em.flush();
        em.clear();

        // then
        EmailPreset savedPreset = em.find(EmailPreset.class, emailPresetId);

        assertThat(savedPreset.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    @Test
    @DisplayName("이메일 프리셋을 수정하면 수정일시가 기록된다.")
    void updateUpdatedAt() {
        // given
        EmailPreset preset = em.find(EmailPreset.class, emailPresetId);
        LocalDateTime originalUpdatedAt = preset.getUpdatedAt();

        // when
        preset.update(
                "배송 시작 안내",
                "[FixGo] 배송 시작 안내",
                "고객님의 제품 배송이 시작되었습니다."
        );

        em.flush();
        em.clear();

        // then
        EmailPreset savedPreset = em.find(EmailPreset.class, emailPresetId);

        assertThat(savedPreset.getUpdatedAt()).isNotNull();
        assertThat(savedPreset.getUpdatedAt())
                .isAfterOrEqualTo(originalUpdatedAt);
    }

    @Test
    @DisplayName("이메일 프리셋을 삭제하면 DB에서 조회되지 않는다.")
    void delete() {
        // given
        EmailPreset preset = em.find(EmailPreset.class, emailPresetId);

        // when
        em.remove(preset);
        em.flush();
        em.clear();

        // then
        EmailPreset deletedPreset = em.find(EmailPreset.class, emailPresetId);

        assertThat(deletedPreset).isNull();
    }
}
