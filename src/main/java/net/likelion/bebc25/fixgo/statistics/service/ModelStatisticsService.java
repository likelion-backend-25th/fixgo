package net.likelion.bebc25.fixgo.statistics.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.statistics.dto.ModelStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.repository.ModelStatisticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModelStatisticsService {

    private final ModelStatisticsRepository modelStatisticsRepository;

    // 모델별 수리 횟수 통계
    public List<ModelStatisticsDto.ModelStatisticsResponse> getModelStatistics() {

        List<ModelStatisticsRepository.ModelStatistics> statistics = modelStatisticsRepository.findModelStatistics(
                AsReceptionStatus.COMPLETED);

        return statistics.stream().map(statistic -> {
            List<ModelStatisticsRepository.ModelSymptomStatistics> symptoms = modelStatisticsRepository.findModelSymptomStatistics(
                    statistic.getModelId());

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
            Long modelId
    ) {
        List<ModelStatisticsRepository.ModelSymptomStatistics> statistics = modelStatisticsRepository.findModelSymptomStatistics(
                modelId);

        long totalCount = statistics.stream().mapToLong(ModelStatisticsRepository.ModelSymptomStatistics::getCount)
                                    .sum();

        return statistics.stream().map(statistic -> {
            double percentage = totalCount == 0 ? 0.0 : statistic.getCount() * 100.0 / totalCount;

            return new ModelStatisticsDto.ModelSymptomStatisticsResponse(statistic.getSymptomName(),
                                                                         statistic.getCount(),
                                                                         Math.round(percentage * 100.0) / 100.0);
        }).toList();
    }
}