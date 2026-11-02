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

        repair = new Repair(WarrantyResult.FREE, RepairStatus.REQUESTED);
        repair = repairRepository.save(repair);

        RepairWork work1 = new RepairWork(repair, "부품 교체", "메인보드 교체", now, now, "tester", "tester");

        RepairWork work2 = new RepairWork(repair, "부품 교체", "메모리 교체", now, now, "tester", "tester");

        RepairWork work3 = new RepairWork(repair, "점검 작업", "메인보드 점검", now, now, "tester", "tester");

        repairWorkRepository.saveAll(List.of(work1, work2, work3));
    }

    @Test
    @DisplayName("A/S 수리 건별 작업 내역 조회")
    void findByRepairId() {

        List<RepairWork> result = repairWorkRepository.findByRepairId(repair.getId());

        assertThat(result).hasSize(3);

        assertThat(result).extracting(RepairWork::getWorkName).containsExactlyInAnyOrder("메인보드 교체", "메모리 교체", "메인보드 점검");
    }

    @Test
    @DisplayName("작업명 일부로 검색")
    void findByWorkName() {

        List<RepairWork> result = repairWorkRepository.findByWorkName("메인보드");

        assertThat(result).hasSize(2);

        assertThat(result).extracting(RepairWork::getWorkName).containsExactlyInAnyOrder("메인보드 교체", "메인보드 점검");
    }

    @Test
    @DisplayName("존재하지 않는 작업명 검색")
    void findByWorkNameNotFound() {

        List<RepairWork> result = repairWorkRepository.findByWorkName("그래픽카드");

        assertThat(result).isEmpty();
    }
}