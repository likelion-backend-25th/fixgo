package net.likelion.bebc25.fixgo.statistics.dto;

public record TechnicianStatisticsDto(Long technicianId, String technicianName, Long assignedCount, Long completedCount,
                                      Long inProgressCount, Double averageDurationMinutes, Long revenue) {}