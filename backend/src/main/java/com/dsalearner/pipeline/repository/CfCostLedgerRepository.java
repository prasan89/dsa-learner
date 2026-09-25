package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCostLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CfCostLedgerRepository extends JpaRepository<CfCostLedger, Long> {

    List<CfCostLedger> findByLessonId(UUID lessonId);

    @Query("SELECT COALESCE(SUM(c.costUsd), 0) FROM CfCostLedger c WHERE c.lessonId = :lessonId")
    BigDecimal sumCostByLessonId(@Param("lessonId") UUID lessonId);

    @Query("SELECT COALESCE(SUM(c.costUsd), 0) FROM CfCostLedger c WHERE CAST(c.recordedAt AS date) = CURRENT_DATE")
    BigDecimal sumTodayCost();
}
