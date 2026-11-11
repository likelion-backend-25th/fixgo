package net.likelion.bebc25.fixgo.as.repository;

import net.likelion.bebc25.fixgo.as.dto.AsHistoryListDto;
import net.likelion.bebc25.fixgo.as.entity.AsTechnicianAssignmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsTechnicianAssignmentHistoryRepository extends JpaRepository<AsTechnicianAssignmentHistory, Long> {

    List<AsTechnicianAssignmentHistory> findByAsReception_IdOrderByCreatedAtDescIdDesc(Long receptionId);

    @Query(value = """
            select new net.likelion.bebc25.fixgo.as.dto.AsHistoryListDto$TechnicianItem(
                h.id,
                r.id,
                r.receptionNumber,
                r.customer.name,
                (select pm.modelName from ProductModel pm where pm.id = r.product.productModelId),
                prev.name,
                next.name,
                h.reason,
                actor.name,
                h.createdAt
            )
            from AsTechnicianAssignmentHistory h
            join h.asReception r
            join h.newTechnician next
            join h.changedBy actor
            left join h.previousTechnician prev
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(next.name) like lower(concat('%', :keyword, '%'))
                or lower(prev.name) like lower(concat('%', :keyword, '%'))
                or lower(h.reason) like lower(concat('%', :keyword, '%')))
            order by h.createdAt desc, h.id desc
            """,
            countQuery = """
            select count(h)
            from AsTechnicianAssignmentHistory h
            join h.asReception r
            join h.newTechnician next
            left join h.previousTechnician prev
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(next.name) like lower(concat('%', :keyword, '%'))
                or lower(prev.name) like lower(concat('%', :keyword, '%'))
                or lower(h.reason) like lower(concat('%', :keyword, '%')))
            """)
    Page<AsHistoryListDto.TechnicianItem> search(
            @Param("keyword") String keyword,
            Pageable pageable
    );

}
