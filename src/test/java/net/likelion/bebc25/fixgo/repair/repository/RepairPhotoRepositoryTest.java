package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.Repair;
import net.likelion.bebc25.fixgo.repair.entity.RepairPhoto;
import net.likelion.bebc25.fixgo.repair.entity.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.WarrantyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
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
        repair = new Repair();
        repair.setWarrantyResult(WarrantyResult.FREE);
        repair.setStatus(RepairStatus.REQUESTED);
        repair.setCreatedAt(now);
        repair.setUpdatedAt(now);
        repair.setCreatedBy("test");
        repair.setUpdatedBy("test");

        repair = repairRepository.save(repair);

        // 테스트용 수리 사진 데이터 생성
        RepairPhoto photo1 = new RepairPhoto();
        photo1.setRepair(repair);
        photo1.setPhotoType("수리 전");
        photo1.setFileUrl("https://example.com/before1.jpg");
        photo1.setCreatedAt(now);
        photo1.setUpdatedAt(now);
        photo1.setCreatedBy("test");
        photo1.setUpdatedBy("test");
        repairPhotoRepository.save(photo1);

        RepairPhoto photo2 = new RepairPhoto();
        photo2.setRepair(repair);
        photo2.setPhotoType("수리 중");
        photo2.setFileUrl("https://example.com/working1.jpg");
        photo2.setCreatedAt(now);
        photo2.setUpdatedAt(now);
        photo2.setCreatedBy("test");
        photo2.setUpdatedBy("test");
        repairPhotoRepository.save(photo2);

        RepairPhoto photo3 = new RepairPhoto();
        photo3.setRepair(repair);
        photo3.setPhotoType("수리 후");
        photo3.setFileUrl("https://example.com/after1.jpg");
        photo3.setCreatedAt(now);
        photo3.setUpdatedAt(now);
        photo3.setCreatedBy("test");
        photo3.setUpdatedBy("test");
        repairPhotoRepository.save(photo3);
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
