package net.likelion.bebc25.fixgo.notification.repository;

import net.likelion.bebc25.fixgo.notification.entity.EmailPreset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailPresetRepository extends JpaRepository<EmailPreset, Long> {

    // 이메일 프리셋 등록 순서대로 조회
    List<EmailPreset> findAllByOrderByIdAsc();


}
