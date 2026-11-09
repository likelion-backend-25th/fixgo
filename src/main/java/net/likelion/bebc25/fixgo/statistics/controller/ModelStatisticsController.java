package net.likelion.bebc25.fixgo.statistics.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.statistics.dto.ModelStatisticsDto;
import net.likelion.bebc25.fixgo.statistics.service.ModelStatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class ModelStatisticsController {

    private final ModelStatisticsService modelStatisticsService;

    // 모델별 수리 통계 조회
    @GetMapping("/models")
    public List<ModelStatisticsDto.ModelStatisticsResponse> getModelStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return modelStatisticsService.getModelStatistics(startDate, endDate);
    }

    // 특정 모델의 증상별 통계 조회
    @GetMapping("/models/{modelId}/symptoms")
    public List<ModelStatisticsDto.ModelSymptomStatisticsResponse> getModelSymptomStatistics(
            @PathVariable Long modelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return modelStatisticsService.getModelSymptomStatistics(modelId, startDate, endDate);
    }
}
