package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.config.JpaConfig;
import net.likelion.bebc25.fixgo.repair.entity.RepairPhoto;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
class RepairPhotoRepositoryTest {

    @Autowired
    private RepairPhotoRepository repairPhotoRepository;

    @Autowired
    private RepairRepository repairRepository;

    private Repair repair;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // 테스트용 A/S 수리 데이터 생성
        repair = new Repair(WarrantyResult.FREE, RepairStatus.REQUESTED);
        repair = repairRepository.save(repair);

        // 테스트용 수리 사진 데이터 생성
        RepairPhoto photo1 = new RepairPhoto(repair, "수리 전", "https://example.com/before1.jpg", now, now, "tester", "tester");

        RepairPhoto photo2 = new RepairPhoto(repair, "수리 중", "https://example.com/working1.jpg", now, now, "tester", "tester");

        RepairPhoto photo3 = new RepairPhoto(repair, "수리 후", "https://example.com/after1.jpg", now, now, "tester", "tester");

        repairPhotoRepository.saveAll(List.of(photo1, photo2, photo3));
    }

    @Test
    @DisplayName("A/S 수리 건별 사진 목록 조회")
    void findByRepairId() {

        // when
        List<RepairPhoto> result = repairPhotoRepository.findByRepairId(repair.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(RepairPhoto::getPhotoType).containsExactlyInAnyOrder("수리 전", "수리 중", "수리 후");
    }

    @Test
    @DisplayName("A/S 수리 건별 사진 유형 조회")
    void findByRepairIdAndPhotoType() {

        // when
        List<RepairPhoto> result = repairPhotoRepository.findByRepairIdAndPhotoType(repair.getId(), "수리 전");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPhotoType()).isEqualTo("수리 전");
        assertThat(result.get(0).getFileUrl()).isEqualTo("https://example.com/before1.jpg");
    }

    @Test
    @DisplayName("존재하지 않는 사진 유형 조회")
    void findByRepairIdAndPhotoTypeNotFound() {

        // when
        List<RepairPhoto> result = repairPhotoRepository.findByRepairIdAndPhotoType(repair.getId(), "기타");

        // then
        assertThat(result).isEmpty();
    }
}
