package net.likelion.bebc25.fixgo.admin.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.admin.dto.LaborRateDto;
import net.likelion.bebc25.fixgo.admin.entity.LaborRate;
import net.likelion.bebc25.fixgo.admin.repository.LaborRateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LaborRateService {

    private final LaborRateRepository laborRateRepository;

    public List<LaborRateDto.Response> getAllLaborRates() {
        return laborRateRepository.findAllByOrderByProductCategoryAscRateTypeAsc().stream()
                .map(LaborRateDto.Response::from)
                .toList();
    }

    public LaborRateDto.Response getLaborRate(Long id) {
        return LaborRateDto.Response.from(getLaborRateEntity(id));
    }

    @Transactional
    public LaborRateDto.Response updateLaborRate(LaborRateDto.UpdateRequest request) {
        LaborRate laborRate = getLaborRateEntity(request.id());
        laborRate.update(request.cost());
        return LaborRateDto.Response.from(laborRate);
    }

    private LaborRate getLaborRateEntity(Long id) {
        return laborRateRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("존재하지 않는 요금 정보입니다.")
        );
    }
}
