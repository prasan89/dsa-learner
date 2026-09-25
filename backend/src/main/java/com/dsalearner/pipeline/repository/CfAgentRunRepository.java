package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfAgentRunRepository extends JpaRepository<CfAgentRun, UUID> {

    List<CfAgentRun> findByLessonIdAndLessonVersion(UUID lessonId, int lessonVersion);

    @Query("""
        SELECT r FROM CfAgentRun r
        WHERE r.lessonId = :lessonId
          AND r.lessonVersion = :lessonVersion
          AND r.agentType = :agentType
          AND r.inputHash = :inputHash
          AND r.status = 'SUCCEEDED'
        ORDER BY r.createdAt DESC
        LIMIT 1
        """)
    Optional<CfAgentRun> findSucceededByIdempotencyKey(
            @Param("lessonId")      UUID lessonId,
            @Param("lessonVersion") int  lessonVersion,
            @Param("agentType")     String agentType,
            @Param("inputHash")     String inputHash
    );
}
