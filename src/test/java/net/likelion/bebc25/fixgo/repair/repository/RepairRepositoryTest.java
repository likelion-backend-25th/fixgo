package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import net.likelion.bebc25.fixgo.repair.entity.repair.RepairStatus;
import net.likelion.bebc25.fixgo.repair.entity.repair.WarrantyResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairRepositoryTest {

    @Autowired
    private RepairRepository repairRepository;

    // 공통 테스트 데이터 생성
    private Repair createRepair(Long asRequestId, Long technicianId) {
        LocalDateTime now = LocalDateTime.now();

        Repair repair = new Repair();

        repair.setAsRequestId(asRequestId);
        repair.setTechnicianId(technicianId);
        repair.setWarrantyResult(WarrantyResult.FREE);
        repair.setStatus(RepairStatus.REQUESTED);
        repair.setCreatedAt(now);
        repair.setUpdatedAt(now);
        repair.setCreatedBy("test");
        repair.setUpdatedBy("test");

        return repair;
    }

    @Test
    @DisplayName("A/S 접수 ID로 수리 정보 조회")
    void findByAsRequestIdTest() {
        // given
        Repair repair = createRepair(1L, 100L);
        repairRepository.save(repair);

        // when
        Optional<Repair> result = repairRepository.findByAsRequestId(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getAsRequestId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("담당 기사별 수리 목록 조회")
    void findByTechnicianIdTest() {
        // given
        Repair repair = createRepair(1L, 100L);
        repairRepository.save(repair);

        // when
        List<Repair> result = repairRepository.findByTechnicianId(100L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnicianId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("수리 상태별 목록 조회")
    void findByStatusTest() {
        // given
        Repair repair = createRepair(1L, 100L);
        repairRepository.save(repair);

        // when
        List<Repair> result = repairRepository.findByStatus(RepairStatus.REQUESTED);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(RepairStatus.REQUESTED);
    }

    @Test
    @DisplayName("담당 기사 및 수리 상태별 목록 조회")
    void findByTechnicianIdAndStatusTest() {
        // given
        Repair repair = createRepair(1L, 100L);
        repairRepository.save(repair);

        // when
        List<Repair> result = repairRepository.findByTechnicianIdAndStatus(100L, RepairStatus.REQUESTED);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTechnicianId()).isEqualTo(100L);
        assertThat(result.get(0).getStatus()).isEqualTo(RepairStatus.REQUESTED);
    }
}