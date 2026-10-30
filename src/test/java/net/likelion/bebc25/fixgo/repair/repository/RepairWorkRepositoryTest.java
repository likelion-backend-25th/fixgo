package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.RepairWork;
import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairWorkRepositoryTest {

    @Autowired
    private RepairWorkRepository repairWorkRepository;

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

        // 테스트용 수리 작업 데이터 생성
        RepairWork work1 = new RepairWork();
        work1.setRepair(repair);
        work1.setRepair_preset_name("부품 교체");
        work1.setWorkName("메인보드 교체");
        work1.setCreatedAt(now);
        work1.setUpdatedAt(now);
        work1.setCreatedBy("test");
        work1.setUpdatedBy("test");
        repairWorkRepository.save(work1);

        RepairWork work2 = new RepairWork();
        work2.setRepair(repair);
        work2.setRepair_preset_name("부품 교체");
        work2.setWorkName("메모리 교체");
        work2.setCreatedAt(now);
        work2.setUpdatedAt(now);
        work2.setCreatedBy("test");
        work2.setUpdatedBy("test");
        repairWorkRepository.save(work2);

        RepairWork work3 = new RepairWork();
        work3.setRepair(repair);
        work3.setRepair_preset_name("점검 작업");
        work3.setWorkName("메인보드 점검");
        work3.setCreatedAt(now);
        work3.setUpdatedAt(now);
        work3.setCreatedBy("test");
        work3.setUpdatedBy("test");
        repairWorkRepository.save(work3);
    }

    @Test
    @DisplayName("A/S 수리 건별 작업 내역 조회")
    void findByRepairId() {
        // when
        List<RepairWork> result = repairWorkRepository.findByRepairId(repair.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(RepairWork::getWorkName).containsExactlyInAnyOrder("메인보드 교체", "메모리 교체", "메인보드 점검");
    }

    @Test
    @DisplayName("작업명 일부로 검색")
    void findByWorkName() {
        // when
        List<RepairWork> result = repairWorkRepository.findByWorkName("메인보드");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(RepairWork::getWorkName).containsExactlyInAnyOrder("메인보드 교체", "메인보드 점검");
    }

    @Test
    @DisplayName("존재하지 않는 작업명 검색")
    void findByWorkNameNotFound() {
        // when
        List<RepairWork> result = repairWorkRepository.findByWorkName("그래픽카드");

        // then
        assertThat(result).isEmpty();
    }
}