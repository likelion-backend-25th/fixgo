package net.likelion.bebc25.fixgo.repair.dto.repairWork;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RepairWorkResponseDto {

    // 작업 내역 ID
    private Long id;

    // 수리 ID
    private Long repairId;

    // 수리 프리셋 이름
    private String repairPresetName;

    // 작업 내용
    private String workName;

    // 생성 일시
    private LocalDateTime createdAt;

    // 수정 일시
    private LocalDateTime updatedAt;

    // 생성자
    private String createdBy;

    // 수정자
    private String updatedBy;
}