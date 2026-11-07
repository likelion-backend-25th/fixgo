package net.likelion.bebc25.fixgo.repair.dto.preset;

import net.likelion.bebc25.fixgo.repair.dto.repair.RepairPresetPartRequest;

import java.util.List;

public record RepairPresetUpdate(
        String name,
        String description,
        List<RepairPresetPartRequest> parts
) {

    // 기존 2개 인자 생성자 호환
    public RepairPresetUpdate(String name, String description) {
        this(name, description, null);
    }
}