package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Wraps all @Modifying repository calls in a separate Spring-managed bean so
 * that @Transactional is applied via proxy (self-calls from the same class are
 * not intercepted by Spring AOP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumJobStore {

    private final CfCurriculumPipelineJobRepository jobRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Optional<CfCurriculumPipelineJob> claim(UUID jobId) {
        int claimed = jobRepository.claimJob(jobId);
        if (claimed == 0) return Optional.empty();
        CfCurriculumPipelineJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null) {
            job.setStartedAt(Instant.now());
            jobRepository.save(job);
        }
        return Optional.ofNullable(job);
    }

    @Transactional
    public void markSucceeded(UUID jobId, String resultRef) {
        jobRepository.markSucceeded(jobId, resultRef);
    }

    @Transactional
    public void markFailed(UUID jobId, String error) {
        jobRepository.markFailed(jobId, abbreviate(error, 2000));
    }

    @Transactional
    public void markRetrying(UUID jobId, String error) {
        jobRepository.markRetrying(jobId, abbreviate(error, 500));
    }

    @Transactional
    public CfCurriculumPipelineJob enqueue(CfCurriculumPipelineJob job) {
        CfCurriculumPipelineJob saved = jobRepository.save(job);
        redisTemplate.opsForList().rightPush(CurriculumJobWorker.QUEUE_KEY, saved.getId().toString());
        log.info("CurriculumJobStore: enqueued jobId={} curriculumId={} type={}",
                saved.getId(), saved.getCurriculumId(), saved.getJobType());
        return saved;
    }

    private String abbreviate(String msg, int max) {
        if (msg == null) return "unknown error";
        return msg.length() > max ? msg.substring(0, max) + "..." : msg;
    }
}
