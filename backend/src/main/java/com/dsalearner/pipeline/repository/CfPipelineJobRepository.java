package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfPipelineJobRepository extends JpaRepository<CfPipelineJob, UUID> {

    Optional<CfPipelineJob> findByLessonIdAndJobTypeAndStatusIn(
            UUID lessonId, String jobType, List<String> statuses);

    /**
     * Atomically claims a job by transitioning it from QUEUED/RETRYING to RUNNING.
     * Returns the number of rows updated: 1 = claimed, 0 = already taken or not found.
     * Incrementing attempt and clearing error in the same UPDATE keeps the record consistent.
     */
    @Modifying
    @Query("""
            UPDATE CfPipelineJob j
            SET j.status = 'RUNNING',
                j.attempt = j.attempt + 1,
                j.startedAt = :now,
                j.error = null
            WHERE j.id = :jobId
              AND j.status IN ('QUEUED', 'RETRYING')
            """)
    int claimJob(@Param("jobId") UUID jobId, @Param("now") Instant now);

    /**
     * Finds jobs that are still QUEUED and were created before the given threshold.
     * Used by the recovery sweep to re-enqueue jobs that were never picked up
     * (e.g. because the Redis RPUSH failed after the DB INSERT).
     */
    @Query("SELECT j FROM CfPipelineJob j WHERE j.status = 'QUEUED' AND j.createdAt < :threshold")
    List<CfPipelineJob> findStaleQueuedJobs(@Param("threshold") Instant threshold);

    /**
     * Finds jobs stuck in RUNNING state past the timeout threshold.
     * These are jobs whose worker thread died (OOM, timeout, crash) without
     * marking the job FAILED/RETRYING — they will never complete on their own.
     */
    @Modifying
    @Query("""
            UPDATE CfPipelineJob j
            SET j.status = 'QUEUED',
                j.attempt = j.attempt - 1,
                j.startedAt = null,
                j.error = 'recovered: stuck in RUNNING past timeout'
            WHERE j.status = 'RUNNING'
              AND j.startedAt < :threshold
              AND j.attempt < j.maxAttempts
            """)
    int resetStuckRunningJobs(@Param("threshold") Instant threshold);
}
