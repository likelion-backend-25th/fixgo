package net.likelion.bebc25.fixgo.notification.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.fixgo.notification.dto.EmailPresetDto;
import net.likelion.bebc25.fixgo.notification.entity.EmailPreset;
import net.likelion.bebc25.fixgo.notification.repository.EmailPresetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmailPresetService {

    private final EmailPresetRepository emailPresetRepository;

    // 이메일 프리셋 등록
    @Transactional
    public EmailPresetDto.Response createPreset(EmailPresetDto.CreateRequest request) {
        EmailPreset preset = EmailPreset.create(request.name(), request.messageTitle(), request.message());

        EmailPreset savedPreset = emailPresetRepository.save(preset);

        return EmailPresetDto.Response.from(savedPreset);
    }

    // 이메일 프리셋 목록 조회 (등록 순)
    public List<EmailPresetDto.Response> getPresetList() {
        return emailPresetRepository.findAllByOrderByIdAsc()
                .stream()
                .map(EmailPresetDto.Response::from)
                .toList();
    }

    // 이메일 프리셋 수정
    @Transactional
    public EmailPresetDto.Response updatePreset(Long id, EmailPresetDto.UpdateRequest request) {
        EmailPreset preset = emailPresetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 프리셋입니다. id: " + id));

        preset.update(request.name(), request.messageTitle(), request.message());

        return EmailPresetDto.Response.from(preset);
    }

    // 이메일 프리셋 삭제
    @Transactional
    public void deletePreset(Long id) {
        EmailPreset preset = emailPresetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 프리셋입니다. id: " + id));

        emailPresetRepository.delete(preset);
    }


}
