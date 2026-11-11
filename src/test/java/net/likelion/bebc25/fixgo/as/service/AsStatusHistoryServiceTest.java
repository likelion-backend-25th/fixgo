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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
    @DisplayName("고객 이력은 접수 상태만 반환하고 수리 완료 이력은 제외한다.")
    void getCustomerHistory_filtersRepairHistory() {
        var initial = new AsStatusHistory(asReception, null, "RECEIVED", "최초 접수", changedBy);
        var repair = new AsStatusHistory(asReception, "REPAIRING", "COMPLETED", "수리 완료", changedBy);
        when(asStatusHistoryRepository.findHistoryByReceptionId(1L))
                .thenReturn(List.of(repair, initial));

        var result = asStatusHistoryService.getCustomerHistory(1L);

        assertEquals(1, result.size());
        assertEquals(AsReceptionStatus.RECEIVED, result.getFirst().status());
        verify(asStatusHistoryRepository).findHistoryByReceptionId(1L);
    }

    @Test
    @DisplayName("이력 검색은 입력을 정리하고 고정된 최신순 정렬을 사용한다.")
    void search_normalizesFiltersAndPreservesPagination() {
        var pageable = PageRequest.of(2, 10, Sort.by("reason"));
        var expectedPage = PageRequest.of(2, 10);
        when(asStatusHistoryRepository.search("AS-123", "RECEIVED", expectedPage))
                .thenReturn(Page.empty(expectedPage));

        var result = asStatusHistoryService.search("  AS-123  ", " RECEIVED ", pageable);

        assertEquals(2, result.getNumber());
        assertEquals(10, result.getSize());
        verify(asStatusHistoryRepository).search("AS-123", "RECEIVED", expectedPage);
    }

    @Test
    @DisplayName("빈 검색 조건은 필터를 적용하지 않는다.")
    void search_blankFilters() {
        var pageable = PageRequest.of(0, 20);

        asStatusHistoryService.search("  ", null, pageable);
        asStatusHistoryService.search(null, "\t", pageable);

        verify(asStatusHistoryRepository, org.mockito.Mockito.times(2))
                .search(null, null, pageable);
    }

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
