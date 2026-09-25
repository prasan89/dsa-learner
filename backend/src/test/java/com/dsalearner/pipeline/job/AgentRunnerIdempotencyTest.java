package com.dsalearner.pipeline.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.language.ContentGenerationInput;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.provider.MockLlmProvider;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
 * Proves that AgentRunner idempotency prevents duplicate LLM calls.
 * Uses the real AgentRunner with mocked repository/cost ledger.
 */
@ExtendWith(MockitoExtension.class)
class AgentRunnerIdempotencyTest {

    @Mock CfAgentRunRepository agentRunRepo;
    @Mock CostLedgerService costLedger;
    @InjectMocks AgentRunner runner;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();

    @Test
    void secondIdenticalCallReturnsCachedOutputWithoutCalllingAgent() {
        UUID existingRunId = UUID.randomUUID();
        CfAgentRun cached = CfAgentRun.builder()
                .id(existingRunId).lessonId(lessonId).lessonVersion(1)
                .agentType(AgentType.CONTENT_GENERATOR)
                .status("SUCCEEDED").output(Map.of("value", "cached-output"))
                .modelConfigKey("mock_v1").build();

        // Simulate cache hit
        when(agentRunRepo.findSucceededByIdempotencyKey(
                eq(lessonId), eq(1), eq(AgentType.CONTENT_GENERATOR), any()))
                .thenReturn(Optional.of(cached));

        boolean[] agentCalled = {false};
        Agent<String, Map<String, Object>> trackingAgent = new Agent<>() {
            @Override public String agentType() { return AgentType.CONTENT_GENERATOR; }
            @Override public AgentOutput<Map<String, Object>> execute(AgentInput<String> input) {
                agentCalled[0] = true;
                return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                        Map.of("value", "fresh-output"), 0.9, List.of(), List.of(), false,
                        new AgentOutput.AgentMetadata("mock_v1", "mock-model", "anthropic",
                                100, 200, 0.01, 50, null, 1, "1.0"));
            }
        };

        AgentOutput<Map<String, Object>> output = runner.run(trackingAgent, lessonId, 1,
                "language", "de", "input", promptId, 1, "mock_v1");

        assertFalse(agentCalled[0], "Agent.execute must NOT be called on cache hit");
        assertEquals(existingRunId, output.agentRunId());
        // No new run record, no cost entry
        verify(agentRunRepo, never()).save(any());
        verifyNoInteractions(costLedger);
    }

    @Test
    void differentInputProducesNewExecution() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        boolean[] agentCalled = {false};
        Agent<String, Map<String, Object>> trackingAgent = new Agent<>() {
            @Override public String agentType() { return AgentType.CONTENT_GENERATOR; }
            @Override public AgentOutput<Map<String, Object>> execute(AgentInput<String> input) {
                agentCalled[0] = true;
                return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                        Map.of("value", "output"), 0.9, List.of(), List.of(), false,
                        new AgentOutput.AgentMetadata("mock_v1", "mock-model", "mock",
                                100, 200, 0.0, 50, null, 1, "1.0"));
            }
        };

        runner.run(trackingAgent, lessonId, 1, "language", "de",
                "different-input", promptId, 1, "mock_v1");

        assertTrue(agentCalled[0], "Agent must be called when no cache hit exists");
    }

    @Test
    void cacheHitProducesNoDuplicateCostEntry() {
        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID()).lessonId(lessonId).lessonVersion(1)
                .agentType(AgentType.CONTENT_GENERATOR).status("SUCCEEDED")
                .output(Map.of("value", "out"))
                .modelConfigKey("mock_v1")
                .estimatedCostUsd(BigDecimal.valueOf(0.05))
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.of(cached));

        runner.run(new SimpleAgent(), lessonId, 1, "language", "de",
                "input", promptId, 1, "mock_v1");

        verifyNoInteractions(costLedger);
    }

    static class SimpleAgent implements Agent<String, Map<String, Object>> {
        @Override public String agentType() { return AgentType.CONTENT_GENERATOR; }
        @Override public AgentOutput<Map<String, Object>> execute(AgentInput<String> input) {
            return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                    Map.of("value", "output"), 0.9, List.of(), List.of(), false,
                    new AgentOutput.AgentMetadata("mock_v1", "mock-model", "mock",
                            100, 200, 0.0, 50, null, 1, "1.0"));
        }
    }
}
