package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentRunnerTest {

    @Mock CfAgentRunRepository agentRunRepo;
    @Mock CostLedgerService costLedger;
    @InjectMocks AgentRunner runner;

    private final UUID lessonId = UUID.randomUUID();
    private final UUID promptId = UUID.randomUUID();

    // ─── Mock agent for testing ───────────────────────────────────────────

    static class EchoAgent implements Agent<String, String> {
        @Override public String agentType() { return "echo_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                    "echo:" + input.payload(), 0.99, List.of(), List.of(), false,
                    new AgentOutput.AgentMetadata("mock_v1","mock-model","mock",
                            10, 20, 0.0, 5, input.context().promptId(),
                            input.context().promptVersion(), "1.0"));
        }
    }

    static class FailingAgent implements Agent<String, String> {
        @Override public String agentType() { return "fail_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            throw new RuntimeException("Agent failure");
        }
    }

    // ─── Tests ────────────────────────────────────────────────────────────

    @Test
    void executesAgentAndCreatesRunRecord() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentOutput<String> output = runner.run(new EchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "mock_v1");

        assertTrue(output.succeeded());
        assertEquals("echo:hello", output.output());

        // Verify two saves: one for RUNNING, one for SUCCEEDED
        verify(agentRunRepo, times(2)).save(any());
    }

    @Test
    void returnsFromCacheOnIdempotencyHit() {
        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID())
                .lessonId(lessonId)
                .lessonVersion(1)
                .agentType("echo_agent")
                .status("SUCCEEDED")
                .output("echo:hello")
                .modelConfigKey("mock_v1")
                .build();
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("echo_agent"), any()))
                .thenReturn(Optional.of(cached));

        AgentOutput<String> output = runner.run(new EchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "mock_v1");

        assertTrue(output.succeeded());
        // Agent.execute must NOT be called — no new save
        verify(agentRunRepo, never()).save(any());
        verifyNoInteractions(costLedger);
    }

    @Test
    void marksRunFailedOnException() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThrows(RuntimeException.class, () ->
                runner.run(new FailingAgent(), lessonId, 1,
                        "language", "de", "input", promptId, 1, "mock_v1"));

        ArgumentCaptor<CfAgentRun> captor = ArgumentCaptor.forClass(CfAgentRun.class);
        verify(agentRunRepo, atLeastOnce()).save(captor.capture());
        // Last save should have FAILED status
        CfAgentRun lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("FAILED", lastSave.getStatus());
        assertNotNull(lastSave.getErrorMessage());
    }

    @Test
    void hashIsDeterministicForSameInput() {
        String h1 = AgentRunner.hash(lessonId, 1, "agent", "payload");
        String h2 = AgentRunner.hash(lessonId, 1, "agent", "payload");
        assertEquals(h1, h2);
    }

    @Test
    void hashDiffersForDifferentPayloads() {
        String h1 = AgentRunner.hash(lessonId, 1, "agent", "payload-A");
        String h2 = AgentRunner.hash(lessonId, 1, "agent", "payload-B");
        assertNotEquals(h1, h2);
    }
}
