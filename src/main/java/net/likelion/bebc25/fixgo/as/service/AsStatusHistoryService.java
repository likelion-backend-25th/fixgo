package net.likelion.bebc25.fixgo.as.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.dto.AsStatusHistoryDto;
import net.likelion.bebc25.fixgo.as.entity.AsReception;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import net.likelion.bebc25.fixgo.as.repository.AsStatusHistoryRepository;
import net.likelion.bebc25.fixgo.auth.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsStatusHistoryService {

    private final AsStatusHistoryRepository asStatusHistoryRepository;

    // 상태 변경 이력 저장
    @Transactional
    public void record(AsReception asReception, String fromStatus, String toStatus, String reason, Member changedBy) {
        AsStatusHistory history = new AsStatusHistory(asReception, fromStatus, toStatus, reason, changedBy);

        asStatusHistoryRepository.save(history);
    }

    // A/S 접수 최초 등록 이력
    @Transactional
    public void recordInitialStatus(AsReception asReception, Member changedBy) {
        record(asReception, null, AsReceptionStatus.RECEIVED.name(), "A/S 최초 접수", changedBy);
    }

    public List<AsStatusHistoryDto> getHistory(Long receptionId) {
        return asStatusHistoryRepository.findHistoryByReceptionId(receptionId)
                .stream()
                .map(AsStatusHistoryDto::from)
                .toList();
    }
}
