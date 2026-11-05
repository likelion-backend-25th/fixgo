package net.likelion.bebc25.fixgo.repair.entity.part;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PartTest {

    @Test
    @DisplayName("부품을 생성하면 이름, 단가, 기준 시간, 재고가 설정된다")
    void create() {
        Part part = new Part("메인보드", 15000, 40, 8);

        assertThat(part.getName()).isEqualTo("메인보드");
        assertThat(part.getUnitPrice()).isEqualTo(15000);
        assertThat(part.getStandardMinutes()).isEqualTo(40);
        assertThat(part.getStockQuantity()).isEqualTo(8);
        assertThat(part.getStatus()).isNull();
    }

    @Test
    @DisplayName("단가를 변경한다")
    void updateUnitPrice() {
        Part part = new Part("메인보드", 15000, 40, 8);

        part.updateUnitPrice(20000);

        assertThat(part.getUnitPrice()).isEqualTo(20000);
    }

    @Test
    @DisplayName("기준 시간을 변경한다")
    void updateStandardMinutes() {
        Part part = new Part("메인보드", 15000, 40, 8);

        part.updateStandardMinutes(25);

        assertThat(part.getStandardMinutes()).isEqualTo(25);
    }

    @Test
    @DisplayName("재고 수량을 변경한다")
    void updateStockQuantity() {
        Part part = new Part("메인보드", 15000, 40, 8);

        part.updateStockQuantity(3);

        assertThat(part.getStockQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("상태를 변경한다")
    void updateStatus() {
        Part part = new Part("메인보드", 15000, 40, 8);

        part.updateStatus(PartStatus.LOW_QUANTITY);

        assertThat(part.getStatus()).isEqualTo(PartStatus.LOW_QUANTITY);
    }
}
