package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.repair.Repair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RepairHistoryRepositoryTest {

    @Autowired
    private RepairHistoryRepository repairHistoryRepository;

    @Test
    @DisplayName("존재하지 않는 A/S ID로 수리 이력을 조회하면 빈 Optional을 반환한다")
    void findByAsReceptionId_NotFound() {

        // given
        Long asReceptionId = -1L;

        // when
        Optional<Repair> result =
                repairHistoryRepository.findByAsReceptionId(asReceptionId);

        // then
        assertThat(result).isEmpty();
    }
}