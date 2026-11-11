package net.likelion.bebc25.fixgo.statistics.dto;

public class ModelStatisticsDto {
    public record ModelStatisticsResponse(Long modelId, String modelName, String productGroup, Long receptionCount,
                                          Long completedCount, Long inProgressCount, String mainSymptom) {}

    public record ModelSymptomStatisticsResponse(String symptomName, Long count, Double percentage) {}
}