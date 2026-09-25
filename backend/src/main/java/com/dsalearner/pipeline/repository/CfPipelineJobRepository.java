package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CfPipelineJobRepository extends JpaRepository<CfPipelineJob, UUID> {
    Optional<CfPipelineJob> findByLessonIdAndJobTypeAndStatusIn(
            UUID lessonId, String jobType, java.util.List<String> statuses);
}
