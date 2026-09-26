package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumPipelineJobRepository extends JpaRepository<CfCurriculumPipelineJob, UUID> {

    List<CfCurriculumPipelineJob> findByCurriculumIdAndStatus(UUID curriculumId, String status);

    List<CfCurriculumPipelineJob> findByCurriculumIdAndJobTypeAndStatus(UUID curriculumId, String jobType, String status);

    Optional<CfCurriculumPipelineJob> findFirstByCurriculumIdAndJobTypeAndStatusIn(
            UUID curriculumId, String jobType, List<String> statuses);

    @Modifying
    @Query("UPDATE CfCurriculumPipelineJob j SET j.status = 'RUNNING', j.attempt = j.attempt + 1, " +
           "j.startedAt = CURRENT_TIMESTAMP WHERE j.id = :id AND j.status IN ('QUEUED', 'RETRYING')")
    int claimJob(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE CfCurriculumPipelineJob j SET j.status = 'SUCCEEDED', j.resultReference = :ref, " +
           "j.completedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    void markSucceeded(@Param("id") UUID id, @Param("ref") String resultReference);

    @Modifying
    @Query("UPDATE CfCurriculumPipelineJob j SET j.status = 'FAILED', j.error = :error, " +
           "j.completedAt = CURRENT_TIMESTAMP WHERE j.id = :id")
    void markFailed(@Param("id") UUID id, @Param("error") String error);

    @Modifying
    @Query("UPDATE CfCurriculumPipelineJob j SET j.status = 'RETRYING', j.error = :error WHERE j.id = :id")
    void markRetrying(@Param("id") UUID id, @Param("error") String error);
}
