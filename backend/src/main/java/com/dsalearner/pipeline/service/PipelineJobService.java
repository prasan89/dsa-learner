package com.dsalearner.pipeline.service;

import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.NotFoundException;
import com.dsalearner.pipeline.job.ContentJobQueue;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineJobService {

    private final CfPipelineJobRepository jobRepository;
    private final ContentJobQueue jobQueue;

    @Transactional
    public CfPipelineJob submitContentGeneration(UUID lessonId, int lessonVersion,
                                                  Map<String, Object> payload, String actor) {
        boolean alreadyActive = jobRepository.findByLessonIdAndJobTypeAndStatusIn(
                lessonId, "CONTENT_GENERATION",
                List.of("QUEUED", "RUNNING", "RETRYING")).isPresent();
        if (alreadyActive) {
            throw new ConflictException(
                    "A CONTENT_GENERATION job is already active for lessonId=" + lessonId);
        }

        CfPipelineJob job = CfPipelineJob.builder()
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .jobType("CONTENT_GENERATION")
                .maxAttempts(3)
                .payload(payload != null ? payload : Map.of())
                .build();

        jobQueue.enqueue(job);
        log.info("PipelineJobService: submitted CONTENT_GENERATION jobId={} lessonId={} by={}",
                job.getId(), lessonId, actor);
        return job;
    }

    @Transactional
    public CfPipelineJob submitQaContent(UUID lessonId, int lessonVersion,
                                          Map<String, Object> payload, String actor) {
        boolean alreadyActive = jobRepository.findByLessonIdAndJobTypeAndStatusIn(
                lessonId, "QA_CONTENT",
                List.of("QUEUED", "RUNNING", "RETRYING")).isPresent();
        if (alreadyActive) {
            throw new ConflictException(
                    "A QA_CONTENT job is already active for lessonId=" + lessonId);
        }

        CfPipelineJob job = CfPipelineJob.builder()
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .jobType("QA_CONTENT")
                .maxAttempts(3)
                .payload(payload != null ? payload : Map.of())
                .build();

        jobQueue.enqueue(job);
        log.info("PipelineJobService: submitted QA_CONTENT jobId={} lessonId={} by={}",
                job.getId(), lessonId, actor);
        return job;
    }

    @Transactional
    public CfPipelineJob submitRevisionGeneration(UUID lessonId, int lessonVersion,
                                                   Map<String, Object> payload, String actor) {
        boolean alreadyActive = jobRepository.findByLessonIdAndJobTypeAndStatusIn(
                lessonId, "REVISION_GENERATION",
                List.of("QUEUED", "RUNNING", "RETRYING")).isPresent();
        if (alreadyActive) {
            throw new ConflictException(
                    "A REVISION_GENERATION job is already active for lessonId=" + lessonId);
        }

        CfPipelineJob job = CfPipelineJob.builder()
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .jobType("REVISION_GENERATION")
                .maxAttempts(3)
                .payload(payload != null ? payload : Map.of())
                .build();

        jobQueue.enqueue(job);
        log.info("PipelineJobService: submitted REVISION_GENERATION jobId={} lessonId={} version={} by={}",
                job.getId(), lessonId, lessonVersion, actor);
        return job;
    }

    public CfPipelineJob getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Pipeline job not found: " + jobId));
    }
}
