package net.likelion.bebc25.fixgo.repair.repository;

import net.likelion.bebc25.fixgo.repair.entity.SymptomPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SymptomPresetRepository extends JpaRepository<SymptomPreset, Long> {

    // 증상명 또는 제품군으로 검색
    // Repository
    @Query("""
            select s from SymptomPreset s
            where lower(s.name) like lower(concat('%', :name, '%'))
               or lower(s.productCategory) like lower(concat('%', :name, '%'))
            order by s.id desc
            """)
    List<SymptomPreset> search(@Param("name") String name);

}