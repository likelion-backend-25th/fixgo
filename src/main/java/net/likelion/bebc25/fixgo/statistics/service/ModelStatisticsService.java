package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ModelStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.ModelStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModelStatisticsService {

    private final ModelStatisticsRepository modelStatisticsRepository;

    // 모델별 수리 횟수 통계
    public List<ModelStatisticsDto.ModelStatisticsResponse> getModelStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startOfPeriod(startDate, endDate);
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ModelStatisticsRepository.ModelStatistics> statistics = modelStatisticsRepository.findModelStatistics(
                AsReceptionStatus.COMPLETED, startDateTime, endDateTime);

        return statistics.stream().map(statistic -> {
            List<ModelStatisticsRepository.ModelSymptomStatistics> symptoms =
                    modelStatisticsRepository.findModelSymptomStatistics(
                            statistic.getModelId(), startDateTime, endDateTime);

            String mainSymptom = symptoms.isEmpty() ? null : symptoms.get(0).getSymptomName();

            return new ModelStatisticsDto.ModelStatisticsResponse(statistic.getModelId(), statistic.getModelName(),
                                                                  statistic.getProductGroup(),
                                                                  statistic.getReceptionCount(),
                                                                  statistic.getCompletedCount(),
                                                                  statistic.getInProgressCount(), mainSymptom);
        }).toList();
    }

    // 특정 모델의 증상별 통계
    public List<ModelStatisticsDto.ModelSymptomStatisticsResponse> getModelSymptomStatistics(
            Long modelId, LocalDate startDate, LocalDate endDate
    ) {
        LocalDateTime startDateTime = startOfPeriod(startDate, endDate);
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ModelStatisticsRepository.ModelSymptomStatistics> statistics =
                modelStatisticsRepository.findModelSymptomStatistics(modelId, startDateTime, endDateTime);

        long totalCount = statistics.stream().mapToLong(ModelStatisticsRepository.ModelSymptomStatistics::getCount)
                                    .sum();

        return statistics.stream().map(statistic -> {
            double percentage = totalCount == 0 ? 0.0 : statistic.getCount() * 100.0 / totalCount;

            return new ModelStatisticsDto.ModelSymptomStatisticsResponse(statistic.getSymptomName(),
                                                                         statistic.getCount(),
                                                                         Math.round(percentage * 100.0) / 100.0);
        }).toList();
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