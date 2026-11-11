package net.likelion.bebc25.fixgo.as.repository;

import net.likelion.bebc25.fixgo.as.dto.AsHistoryListDto;
import net.likelion.bebc25.fixgo.as.entity.AsStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsStatusHistoryRepository extends JpaRepository<AsStatusHistory, Long> {
    @Query("""
                SELECT h
                FROM AsStatusHistory h
                WHERE h.asReception.id = :receptionId
                ORDER BY h.createdAt DESC
            """)
    List<AsStatusHistory> findHistoryByReceptionId(@Param("receptionId") Long receptionId);

    @Query(value = """
            select new net.likelion.bebc25.fixgo.as.dto.AsHistoryListDto$StatusItem(
                h.id,
                r.id,
                r.receptionNumber,
                r.customer.name,
                (select pm.modelName from ProductModel pm where pm.id = r.product.productModelId),
                h.fromStatus,
                h.toStatus,
                h.reason,
                actor.name,
                h.createdAt
            )
            from AsStatusHistory h
            join h.asReception r
            left join h.changedBy actor
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(h.reason) like lower(concat('%', :keyword, '%')))
              and (:toStatus is null or h.toStatus = :toStatus)
            order by h.createdAt desc, h.id desc
            """,
            countQuery = """
            select count(h)
            from AsStatusHistory h
            join h.asReception r
            where (:keyword is null
                or lower(r.receptionNumber) like lower(concat('%', :keyword, '%'))
                or lower(r.customer.name) like lower(concat('%', :keyword, '%'))
                or lower(h.reason) like lower(concat('%', :keyword, '%')))
              and (:toStatus is null or h.toStatus = :toStatus)
            """)
    Page<AsHistoryListDto.StatusItem> search(
            @Param("keyword") String keyword,
            @Param("toStatus") String toStatus,
            Pageable pageable
    );
}
