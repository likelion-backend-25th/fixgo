package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.part.PartStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartStockHistoryRepository extends JpaRepository<PartStockHistory, Long> {
}
