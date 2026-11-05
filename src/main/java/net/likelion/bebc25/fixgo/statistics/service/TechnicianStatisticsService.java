package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.auth.entity.Role;
import net.likelion.bebc25.fixgo.payment.entity.PaymentAttemptStatus;
import net.likelion.bebc25.fixgo.statistics.dto.TechnicianStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.TechnicianStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechnicianStatisticsService {

    private final TechnicianStatisticsRepository technicianStatisticsRepository;

    public List<TechnicianStatisticsDto> getTechnicianStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startOfPeriod(startDate, endDate);
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        // 기사별 배정, 완료, 진행 건수 조회
        List<TechnicianStatisticsRepository.TechnicianStatistics> statistics = technicianStatisticsRepository.findTechnicianStatistics(
                Role.ROLE_TECHNICIAN, AsReceptionStatus.COMPLETED, startDateTime, endDateTime);

        // 기사별 승인 완료 매출 조회
        Map<Long, Long> revenueMap = technicianStatisticsRepository.findTechnicianRevenue(PaymentAttemptStatus.DONE,
                                                                                         startDateTime, endDateTime)
                                                                   .stream().collect(
                        Collectors.toMap(TechnicianStatisticsRepository.TechnicianRevenue::getTechnicianId,
                                         TechnicianStatisticsRepository.TechnicianRevenue::getRevenue));

        // 평균 소요 시간 조회
        Map<Long, Double> durationMap = technicianStatisticsRepository.findTechnicianAverageDurations(startDateTime,
                                                                                                     endDateTime)
                                                                      .stream()
                                                                      .collect(Collectors.toMap(
                                                                              TechnicianStatisticsRepository.TechnicianAverageDuration::getTechnicianId,
                                                                              result -> result.getAverageDurationSeconds() / 60.0));

        // 통계 데이터 결합
        return statistics.stream().map(statistic -> new TechnicianStatisticsDto(statistic.getTechnicianId(),
                                                                                statistic.getTechnicianName(),
                                                                                statistic.getAssignedCount(),
                                                                                statistic.getCompletedCount(),
                                                                                statistic.getInProgressCount(),
                                                                                durationMap.get(
                                                                                        statistic.getTechnicianId()),
                                                                                revenueMap.getOrDefault(
                                                                                        statistic.getTechnicianId(),
                                                                                        0L))).toList();


    }

    // 조회 기간은 시작일 00:00 이상, 종료일 다음날 00:00 미만
    private LocalDateTime startOfPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("조회 시작일과 종료일은 필수입니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 늦을 수 없습니다.");
        }

        return startDate.atStartOfDay();
    }
}