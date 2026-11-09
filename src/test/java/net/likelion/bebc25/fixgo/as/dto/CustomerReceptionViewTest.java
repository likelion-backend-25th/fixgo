package net.likelion.bebc25.fixgo.as.dto;

import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("고객 포탈 진행이력 변환 테스트")
class CustomerReceptionViewTest {

    private static AsStatusHistory history(String from, String to) {
        return new AsStatusHistory(null, from, to, null, null);
    }

    @Test
    @DisplayName("최초 접수 이력(from 없음)은 보여 준다.")
    void initialReception() {
        assertThat(AsReceptionDto.CustomerReceptionView.StatusHistoryItem.from(history(null, "RECEIVED")))
                .get()
                .extracting(AsReceptionDto.CustomerReceptionView.StatusHistoryItem::status)
                .isEqualTo(AsReceptionStatus.RECEIVED);
    }

    @Test
    @DisplayName("접수 상태 전환 이력은 보여 준다.")
    void receptionTransition() {
        assertThat(AsReceptionDto.CustomerReceptionView.StatusHistoryItem.from(history("AWAITING_REPAIR", "REPAIR_IN_PROGRESS")))
                .get()
                .extracting(AsReceptionDto.CustomerReceptionView.StatusHistoryItem::status)
                .isEqualTo(AsReceptionStatus.REPAIR_IN_PROGRESS);
    }

    @Test
    @DisplayName("수리 상태 이력은 숨긴다.")
    void repairTransitionHidden() {
        assertThat(AsReceptionDto.CustomerReceptionView.StatusHistoryItem.from(history("APPROVED", "REPAIRING"))).isEmpty();
        assertThat(AsReceptionDto.CustomerReceptionView.StatusHistoryItem.from(history("DIAGNOSING", "REQUESTED"))).isEmpty();
    }

    @Test
    @DisplayName("수리 완료(REPAIRING → COMPLETED)는 이름이 같아도 접수 완료로 보이지 않는다.")
    void repairCompletedHidden() {
        assertThat(AsReceptionDto.CustomerReceptionView.StatusHistoryItem.from(history("REPAIRING", "COMPLETED"))).isEmpty();
    }
}
