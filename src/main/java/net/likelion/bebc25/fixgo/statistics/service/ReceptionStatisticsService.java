package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ReceptionStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.ReceptionStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceptionStatisticsService {

    private final ReceptionStatisticsRepository receptionStatisticsRepository;

    // 기간별 접수 건수, 완료 건수, 완료율 조회
    public ReceptionStatisticsDto getReceptionStatistics(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 시작일과 종료일은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        ReceptionStatisticsRepository.ReceptionStatistics statistics = receptionStatisticsRepository.countByPeriod(
                AsReceptionStatus.COMPLETED, startDateTime, endDateTime);

        Long receivedCount = statistics.getReceivedCount();
        Long completedCount = statistics.getCompletedCount();

        return new ReceptionStatisticsDto(startDate, endDate, receivedCount, completedCount,
                                          completionRate(receivedCount, completedCount));
    }

    // 완료율(%). 접수가 없으면 0
    private BigDecimal completionRate(Long receivedCount, Long completedCount) {
        if (receivedCount == 0L) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(completedCount).multiply(BigDecimal.valueOf(100))
                         .divide(BigDecimal.valueOf(receivedCount), 1, RoundingMode.HALF_UP);
    }
}