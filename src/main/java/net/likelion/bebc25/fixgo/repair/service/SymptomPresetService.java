package net.likelion.bebc25.fixgo.repair.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.repair.dto.SymptomPresetResponse;
import net.likelion.bebc25.fixgo.repair.repository.SymptomPresetRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SymptomPresetService {

    private final SymptomPresetRepository symptomPresetRepository;

    // 전체 조회
    public List<SymptomPresetResponse> getSymptomPresets() {
        return symptomPresetRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(SymptomPresetResponse::from)
                .toList();
    }

    // 검색 (증상명, 제품군)
    // Service
    public List<SymptomPresetResponse> searchSymptomPresets(String name) {
        return symptomPresetRepository.search(name.trim())
                .stream()
                .map(SymptomPresetResponse::from)
                .toList();
    }
}