package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentJobQueueTest {

    @Mock CfPipelineJobRepository jobRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ListOperations<String, String> listOps;

    @InjectMocks RedisContentJobQueue queue;

    @Test
    void enqueueSavesJobAndPushesIdToRedis() {
        CfPipelineJob job = CfPipelineJob.builder()
                .lessonId(UUID.randomUUID()).lessonVersion(1)
                .jobType("CONTENT_GENERATION")
                .payload(Map.of("topic", "Greetings"))
                .build();

        when(jobRepository.save(any())).thenAnswer(inv -> {
            CfPipelineJob j = inv.getArgument(0);
            // Simulate DB-assigned UUID
            CfPipelineJob saved = CfPipelineJob.builder()
                    .id(UUID.randomUUID())
                    .lessonId(j.getLessonId()).lessonVersion(j.getLessonVersion())
                    .jobType(j.getJobType()).payload(j.getPayload())
                    .build();
            return saved;
        });
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        UUID jobId = queue.enqueue(job);

        assertNotNull(jobId);
        verify(jobRepository).save(any());
        verify(listOps).rightPush(eq(RedisContentJobQueue.QUEUE_KEY), any());
    }

    @Test
    void enqueueReturnsSavedJobId() {
        UUID expectedId = UUID.randomUUID();
        CfPipelineJob job = CfPipelineJob.builder()
                .lessonId(UUID.randomUUID()).lessonVersion(1)
                .jobType("CONTENT_GENERATION")
                .payload(Map.of())
                .build();

        CfPipelineJob savedJob = CfPipelineJob.builder()
                .id(expectedId)
                .lessonId(job.getLessonId()).lessonVersion(1)
                .jobType("CONTENT_GENERATION").payload(Map.of())
                .build();

        when(jobRepository.save(any())).thenReturn(savedJob);
        when(redisTemplate.opsForList()).thenReturn(listOps);
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        UUID returnedId = queue.enqueue(job);
        assertEquals(expectedId, returnedId);
    }

    @Test
    void jobStatusConstantsAreCorrect() {
        // Verify the queue key is stable — changing it would lose in-flight jobs
        assertEquals("cf:pipeline:jobs", RedisContentJobQueue.QUEUE_KEY);
    }
}
