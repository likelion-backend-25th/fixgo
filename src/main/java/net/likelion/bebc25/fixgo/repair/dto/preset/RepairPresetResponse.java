package net.likelion.bebc25.fixgo.repair.dto.preset;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RepairPresetResponse {

    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    private Long basePrice;

    public RepairPresetResponse(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy, Long basePrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.basePrice = basePrice;
    }
}