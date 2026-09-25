package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.exception.InvalidTransitionException;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.provider.LlmProviderException;
import com.dsalearner.pipeline.repository.CfPipelineJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentJobWorkerTest {

    @Mock CfPipelineJobRepository jobRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock ContentGenerationOrchestrator orchestrator;
    @Mock ListOperations<String, String> listOps;

    @InjectMocks ContentJobWorker worker;

    private final UUID lessonId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForList()).thenReturn(listOps);
    }

    @Test
    void pollDoesNothingWhenQueueIsEmpty() {
        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(null);
        worker.poll();
        verify(jobRepository, never()).findById(any());
    }

    @Test
    void successfulExecutionMarksJobSucceeded() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "QUEUED", 0, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orchestrator.execute(any())).thenReturn("run-ref-123");

        worker.poll();

        ArgumentCaptor<CfPipelineJob> captor = ArgumentCaptor.forClass(CfPipelineJob.class);
        verify(jobRepository, atLeast(2)).save(captor.capture());

        List<CfPipelineJob> saves = captor.getAllValues();
        CfPipelineJob finalState = saves.get(saves.size() - 1);
        assertEquals("SUCCEEDED", finalState.getStatus());
        assertEquals("run-ref-123", finalState.getResultReference());
        assertNotNull(finalState.getCompletedAt());
    }

    @Test
    void transientFailureMovesToRetrying() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "QUEUED", 0, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orchestrator.execute(any())).thenThrow(new LlmProviderException("timeout", true));
        when(listOps.rightPush(any(), any())).thenReturn(1L);

        worker.poll();

        ArgumentCaptor<CfPipelineJob> captor = ArgumentCaptor.forClass(CfPipelineJob.class);
        verify(jobRepository, atLeast(1)).save(captor.capture());
        CfPipelineJob finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("RETRYING", finalState.getStatus());
        assertNotNull(finalState.getError());
        // Must re-enqueue
        verify(listOps).rightPush(eq(RedisContentJobQueue.QUEUE_KEY), eq(jobId.toString()));
    }

    @Test
    void maxAttemptsReachedMarksFailed() {
        UUID jobId = UUID.randomUUID();
        // attempt == maxAttempts → no more retries
        CfPipelineJob job = buildJob(jobId, "RETRYING", 3, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orchestrator.execute(any())).thenThrow(new LlmProviderException("timeout", true));

        worker.poll();

        ArgumentCaptor<CfPipelineJob> captor = ArgumentCaptor.forClass(CfPipelineJob.class);
        verify(jobRepository, atLeast(1)).save(captor.capture());
        CfPipelineJob finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("FAILED", finalState.getStatus());
        // Must NOT re-enqueue
        verify(listOps, never()).rightPush(any(), any());
    }

    @Test
    void nonRetryableErrorMarksFailed() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "QUEUED", 0, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orchestrator.execute(any())).thenThrow(
                new InvalidTransitionException("Invalid state transition"));

        worker.poll();

        ArgumentCaptor<CfPipelineJob> captor = ArgumentCaptor.forClass(CfPipelineJob.class);
        verify(jobRepository, atLeast(1)).save(captor.capture());
        CfPipelineJob finalState = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("FAILED", finalState.getStatus());
        verify(listOps, never()).rightPush(any(), any());
    }

    @Test
    void duplicateDeliverySkippedWhenJobAlreadyRunning() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "RUNNING", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        worker.poll();

        // Orchestrator must not be called
        verify(orchestrator, never()).execute(any());
        // No status change
        verify(jobRepository, never()).save(any());
    }

    @Test
    void duplicateDeliverySkippedWhenJobAlreadySucceeded() {
        UUID jobId = UUID.randomUUID();
        CfPipelineJob job = buildJob(jobId, "SUCCEEDED", 1, 3);

        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn(jobId.toString());
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        worker.poll();

        verify(orchestrator, never()).execute(any());
        verify(jobRepository, never()).save(any());
    }

    @Test
    void invalidUuidOnQueueIsSkipped() {
        when(listOps.leftPop(any(), anyLong(), any(TimeUnit.class))).thenReturn("not-a-uuid");
        worker.poll();
        verify(jobRepository, never()).findById(any());
    }

    private CfPipelineJob buildJob(UUID id, String status, int attempt, int maxAttempts) {
        return CfPipelineJob.builder()
                .id(id).lessonId(lessonId).lessonVersion(1)
                .jobType("CONTENT_GENERATION")
                .status(status).attempt(attempt).maxAttempts(maxAttempts)
                .payload(Map.of("topic", "Greetings"))
                .build();
    }
}
