package net.likelion.bebc25.fixgo.repair.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetRequest;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetResponse;
import net.likelion.bebc25.fixgo.repair.dto.preset.RepairPresetUpdate;
import net.likelion.bebc25.fixgo.repair.entity.RepairPreset;
import net.likelion.bebc25.fixgo.repair.mapper.RepairPresetMapper;
import net.likelion.bebc25.fixgo.repair.repository.RepairPresetRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepairPresetService {

    private final RepairPresetRepository repairPresetRepository;
    private final RepairPresetMapper repairPresetMapper;
    private final EntityManager entityManager;

    // 프리셋 등록
    @Transactional
    public RepairPresetResponse createPreset(RepairPresetRequest request) {
        LocalDateTime now = LocalDateTime.now();

        RepairPreset preset = new RepairPreset(request.getName(), request.getDescription(), now, now, request.getCreatedBy(), request.getCreatedBy());

        RepairPreset savedPreset = repairPresetRepository.save(preset);

        return toResponseDto(savedPreset);
    }

    // 전체 프리셋 조회
    public List<RepairPresetResponse> getAllPresets() {
        return repairPresetRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    // 프리셋 단건 조회
    public RepairPresetResponse getPreset(Long id) {
        RepairPreset preset = findPreset(id);
        return toResponseDto(preset);
    }

    // 프리셋 수정
    @Transactional
    public RepairPresetResponse updatePreset(Long id, RepairPresetUpdate request) {
        findPreset(id);

        LocalDateTime now = LocalDateTime.now();

        repairPresetRepository.updatePreset(id, request.getName(), request.getDescription(), now, request.getUpdatedBy());

        return toResponseDto(findPreset(id));
    }

    // 프리셋 삭제
    @Transactional
    public void deletePreset(Long id) {
        RepairPreset preset = findPreset(id);
        repairPresetRepository.delete(preset);
    }

    // 프리셋 조회 공통 메서드
    private RepairPreset findPreset(Long id) {
        return repairPresetRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 수리 프리셋을 찾을 수 없습니다. id=" + id));
    }

    // 프리셋에 연결된 부품 단가 합산
    private Long calculateBasePrice(Long presetId) {
        Object result = entityManager.createNativeQuery("""
                                                                SELECT COALESCE(SUM(p.unit_price), 0)
                                                                FROM repair_preset_part rpp
                                                                JOIN part p ON p.id = rpp.part_id
                                                                WHERE rpp.repair_preset_id = ?
                                                                """).setParameter(1, presetId).getSingleResult();

        return ((Number) result).longValue();
    }

    // Entity -> Response DTO 변환
    private RepairPresetResponse toResponseDto(RepairPreset preset) {
        Long basePrice = calculateBasePrice(preset.getId());
        return repairPresetMapper.toResponse(preset, basePrice);
    }
}
