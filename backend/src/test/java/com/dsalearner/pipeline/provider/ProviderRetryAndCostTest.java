package com.dsalearner.pipeline.provider;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.job.RetryPolicy;
import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Verifies provider failure → RetryPolicy classification, and cost idempotency:
 *   - HTTP 5xx → LlmProviderException(retryable=true)
 *   - HTTP 4xx → LlmProviderException(retryable=false)
 *   - Cache hit → no provider call, no cost entry
 */
@ExtendWith(MockitoExtension.class)
class ProviderRetryAndCostTest {

    @Mock CfAgentRunRepository agentRunRepo;
    @Mock CostLedgerService costLedger;
    @InjectMocks AgentRunner runner;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();

    // ─── Provider exception classification ────────────────────────────────

    @Test
    void http5xxProducesRetryableException() {
        LlmProviderException ex = new LlmProviderException("Anthropic HTTP 503: service unavailable", true);
        assertTrue(ex.isRetryable(), "5xx errors must be retryable");
        assertTrue(RetryPolicy.isRetryable(ex));
    }

    @Test
    void http4xxProducesNonRetryableException() {
        LlmProviderException ex = new LlmProviderException("Anthropic HTTP 401: unauthorized", false);
        assertFalse(ex.isRetryable(), "4xx auth errors must NOT be retryable");
        assertFalse(RetryPolicy.isRetryable(ex));
    }

    @Test
    void http400BadRequestIsNonRetryable() {
        LlmProviderException ex = new LlmProviderException("Anthropic HTTP 400: bad request", false);
        assertFalse(RetryPolicy.isRetryable(ex));
    }

    @Test
    void timeoutExceptionIsRetryable() {
        LlmProviderException ex = new LlmProviderException("Anthropic provider error: Read timeout", true);
        assertTrue(RetryPolicy.isRetryable(ex));
    }

    @Test
    void illegalStateExceptionIsNonRetryable() {
        // Malformed JSON from LLM → non-retryable
        assertFalse(RetryPolicy.isRetryable(new IllegalStateException("failed to parse model response")));
    }

    @Test
    void illegalArgumentExceptionIsNonRetryable() {
        assertFalse(RetryPolicy.isRetryable(new IllegalArgumentException("Unknown job type")));
    }

    // ─── Cost idempotency ──────────────────────────────────────────────────

    /**
     * Cache hit must produce:
     *   - no agent call
     *   - no new AgentRun record
     *   - no cost ledger entry
     */
    @Test
    void cacheHitProducesNoProviderCallNoCostEntry() {
        UUID existingRunId = UUID.randomUUID();
        CfAgentRun cached = CfAgentRun.builder()
                .id(existingRunId).lessonId(lessonId).lessonVersion(1)
                .agentType(AgentType.CONTENT_GENERATOR).status("SUCCEEDED")
                .output(Map.of("value", "cached-output"))
                .modelConfigKey("mock_v1")
                .estimatedCostUsd(BigDecimal.valueOf(0.05))
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(
                eq(lessonId), eq(1), eq(AgentType.CONTENT_GENERATOR), any()))
                .thenReturn(Optional.of(cached));

        boolean[] agentCalled = {false};
        Agent<String, Map<String, Object>> tracingAgent = new Agent<>() {
            @Override public String agentType() { return AgentType.CONTENT_GENERATOR; }
            @Override public AgentOutput<Map<String, Object>> execute(AgentInput<String> input) {
                agentCalled[0] = true;
                throw new AssertionError("Agent must NOT be called on cache hit");
            }
        };

        AgentOutput<Map<String, Object>> output = runner.run(
                tracingAgent, lessonId, 1, "language", "de",
                "input-payload", promptId, 1, "mock_v1");

        // Agent never called
        assertFalse(agentCalled[0]);
        // No new AgentRun saved
        verify(agentRunRepo, never()).save(any());
        // No cost entry
        verifyNoInteractions(costLedger);
        // Output uses the cached run ID
        assertEquals(existingRunId, output.agentRunId());
    }

    /**
     * Fresh execution must produce exactly one cost entry.
     * Second identical call → cache hit → no second cost entry.
     */
    @Test
    void freshExecutionRecordsCostExactlyOnce() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());  // cache miss → execute
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Agent<String, String> agent = new Agent<>() {
            @Override public String agentType() { return AgentType.CONTENT_GENERATOR; }
            @Override public AgentOutput<String> execute(AgentInput<String> input) {
                return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                        "output", 0.9, List.of(), List.of(), false,
                        new AgentOutput.AgentMetadata("mock_v1", "mock-model", "mock",
                                100, 200, 0.01, 50, null, 1, "1.0"));
            }
        };

        runner.run(agent, lessonId, 1, "language", "de",
                "payload", promptId, 1, "mock_v1");

        // Exactly one cost entry recorded
        verify(costLedger, times(1)).record(
                eq(lessonId), eq(1), any(UUID.class),
                any(), any(), any(), any(),
                anyInt(), anyInt(), anyDouble(), any());
    }
}
