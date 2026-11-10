package net.likelion.bebc25.fixgo.repair.repository;

import jakarta.persistence.EntityManager;
import net.likelion.bebc25.fixgo.repair.entity.SymptomPreset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SymptomPresetRepositoryTest {

    @Autowired
    private SymptomPresetRepository symptomPresetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("증상명으로 프리셋을 검색한다")
    void searchBySymptomName() {
        // given
        entityManager.persist(new SymptomPreset("스마트폰", "화면 파손"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<SymptomPreset> result =
                symptomPresetRepository.search("화면");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("화면 파손");
        assertThat(result.get(0).getProductCategory()).isEqualTo("스마트폰");
    }

    @Test
    @DisplayName("제품군으로 프리셋을 검색한다")
    void searchByProductCategory() {
        // given
        entityManager.persist(new SymptomPreset("노트북", "전원 불량"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<SymptomPreset> result =
                symptomPresetRepository.search("노트북");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductCategory()).isEqualTo("노트북");
    }

    @Test
    @DisplayName("대소문자 구분 없이 검색한다")
    void searchCaseInsensitive() {
        // given
        entityManager.persist(new SymptomPreset("Laptop", "Power Failure"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<SymptomPreset> result =
                symptomPresetRepository.search("POWER");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Power Failure");
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 목록을 반환한다")
    void searchNoResult() {
        // given
        entityManager.persist(new SymptomPreset("스마트폰", "화면 파손"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<SymptomPreset> result =
                symptomPresetRepository.search("배터리");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("검색 결과는 ID 내림차순으로 정렬된다")
    void searchOrderByIdDesc() {
        // given
        SymptomPreset first =
                new SymptomPreset("스마트폰", "화면 파손");
        SymptomPreset second =
                new SymptomPreset("노트북", "화면 불량");

        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        Long firstId = first.getId();
        Long secondId = second.getId();

        entityManager.clear();

        // when
        List<SymptomPreset> result =
                symptomPresetRepository.search("화면");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(secondId);
        assertThat(result.get(1).getId()).isEqualTo(firstId);
    }
}
