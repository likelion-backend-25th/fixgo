package net.likelion.bebc25.fixgo.statistics.repository;

import net.likelion.bebc25.fixgo.as.entity.AsReceptionStatus;
import net.likelion.bebc25.fixgo.product.entity.ProductModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelStatisticsRepository extends Repository<ProductModel, Long> {

    @Query("""
            SELECT
                pm.id AS modelId,
                pm.modelName AS modelName,
                pm.category AS productGroup,
                COUNT(ar) AS receptionCount,
                cast(sum(
                    case when ar.status = :completedStatus then 1 else 0 end
                ) as long) as completedCount,
                cast(sum(
                    case when ar.id is not null and ar.status <> :completedStatus then 1 else 0 end
                ) as long) as inProgressCount
            FROM ProductModel pm
            LEFT JOIN Product p ON p.productModelId = pm.id
            LEFT JOIN AsReception ar ON ar.product = p
            GROUP BY pm.id, pm.modelName, pm.category
            ORDER BY pm.modelName
            """)
    List<ModelStatistics> findModelStatistics(
            @Param("completedStatus") AsReceptionStatus completedStatus
    );

    @Query("""
            SELECT
                ar.symptom AS symptomName,
                COUNT(ar) AS count
            FROM ProductModel pm
            JOIN Product p ON p.productModelId = pm.id
            JOIN AsReception ar ON ar.product = p
            WHERE pm.id = :modelId
            GROUP BY ar.symptom
            ORDER BY COUNT(ar) DESC, ar.symptom ASC
            """)
    List<ModelSymptomStatistics> findModelSymptomStatistics(
            @Param("modelId") Long modelId
    );

    interface ModelStatistics {
        Long getModelId();

        String getModelName();

        String getProductGroup();

        Long getReceptionCount();

        Long getCompletedCount();

        Long getInProgressCount();
    }

    interface ModelSymptomStatistics {
        String getSymptomName();

        Long getCount();
    }
}