package net.likelion.bebc25.fixgo.as.service;

import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("A/S 상태 변경 이력 서비스 테스트")
class AsStatusHistoryServiceTest {

    @Mock
    private AsStatusHistoryRepository asStatusHistoryRepository;

    @Mock
    private AsReception asReception;

    @Mock
    private Member changedBy;

    @InjectMocks
    private AsStatusHistoryService asStatusHistoryService;

    @Test
    @DisplayName("상태 변경 이력을 저장한다.")
    void record_success() {
        // given
        String fromStatus = "RECEIVED";
        String toStatus = "INSPECTION";
        String reason = "제품 점검 시작";

        // when
        asStatusHistoryService.record(asReception, fromStatus, toStatus, reason, changedBy);

        // then
        ArgumentCaptor<AsStatusHistory> captor = ArgumentCaptor.forClass(AsStatusHistory.class);

        verify(asStatusHistoryRepository).save(captor.capture());

        AsStatusHistory history = captor.getValue();

        assertEquals(asReception, history.getAsReception());
        assertEquals(fromStatus, history.getFromStatus());
        assertEquals(toStatus, history.getToStatus());
        assertEquals(reason, history.getReason());
        assertEquals(changedBy, history.getChangedBy());
    }

    @Test
    @DisplayName("최초 접수 이력을 저장한다.")
    void recordInitialStatus_success() {
        // when
        asStatusHistoryService.recordInitialStatus(asReception, changedBy);

        // then
        ArgumentCaptor<AsStatusHistory> captor = ArgumentCaptor.forClass(AsStatusHistory.class);

        verify(asStatusHistoryRepository).save(captor.capture());

        AsStatusHistory history = captor.getValue();

        assertEquals(asReception, history.getAsReception());
        assertNull(history.getFromStatus());
        assertEquals(AsReceptionStatus.RECEIVED.name(), history.getToStatus());
        assertEquals("A/S 최초 접수", history.getReason());
        assertEquals(changedBy, history.getChangedBy());
    }

    @Test
    @DisplayName("A/S 접수 상태 변경 이력을 조회한다.")
    void getHistory_success() {
        // given
        Long receptionId = 1L;

        AsStatusHistory history = new AsStatusHistory(asReception, "RECEIVED", "INSPECTION", "제품 점검 시작", changedBy);

        when(asStatusHistoryRepository.findHistoryByReceptionId(receptionId)).thenReturn(List.of(history));

        when(changedBy.getName()).thenReturn("테스트 직원");

        // when
        List<AsStatusHistoryDto> result = asStatusHistoryService.getHistory(receptionId);

        // then
        assertEquals(1, result.size());
        assertEquals("RECEIVED", result.get(0).fromStatus());
        assertEquals("INSPECTION", result.get(0).toStatus());
        assertEquals("제품 점검 시작", result.get(0).reason());
        assertEquals("테스트 직원", result.get(0).changedBy());

        verify(asStatusHistoryRepository).findHistoryByReceptionId(receptionId);
    }

    @Test
    @DisplayName("상태 변경 이력이 없으면 빈 목록을 반환한다.")
    void getHistory_empty() {
        // given
        Long receptionId = 1L;

        when(asStatusHistoryRepository.findHistoryByReceptionId(receptionId)).thenReturn(List.of());

        // when
        List<AsStatusHistoryDto> result = asStatusHistoryService.getHistory(receptionId);

        // then
        assertEquals(0, result.size());

        verify(asStatusHistoryRepository).findHistoryByReceptionId(receptionId);
    }
}
