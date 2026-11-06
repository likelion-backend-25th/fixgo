package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.statistics.dto.LongPendingStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.LongPendingStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LongPendingStatisticsService {

    private static final int LONG_PENDING_DAYS = 14;

    private final LongPendingStatisticsRepository longPendingStatisticsRepository;

    // 장기 미처리 접수 조회
    public List<LongPendingStatisticsDto.LongPendingResponse> getLongPendingReceptions() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.minusDays(LONG_PENDING_DAYS);

        List<LongPendingStatisticsRepository.LongPendingStatistics> statistics = longPendingStatisticsRepository.findLongPendingReceptions(
                AsReceptionStatus.COMPLETED, threshold);

        return statistics.stream().map(statistic -> {
            long elapsedDays = ChronoUnit.DAYS.between(statistic.getStatusChangedAt().toLocalDate(), now.toLocalDate());

            return new LongPendingStatisticsDto.LongPendingResponse(statistic.getReceptionNumber(),
                                                                    statistic.getCustomerName(),
                                                                    statistic.getModelName(), statistic.getStatus(),
                                                                    statistic.getTechnicianName(), elapsedDays,
                                                                    statistic.getStatusChangedAt());
        }).toList();
    }
}