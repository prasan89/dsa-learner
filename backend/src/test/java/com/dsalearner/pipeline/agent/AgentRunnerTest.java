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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
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

    // ─── Mock agents ──────────────────────────────────────────────────────

    static class EchoAgent implements Agent<String, String> {
        @Override public String agentType() { return "echo_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                    "echo:" + input.payload(), 0.99, List.of(), List.of(), false,
                    new AgentOutput.AgentMetadata("mock_v1", "mock-model", "mock",
                            10, 20, 0.0, 5, input.context().promptId(),
                            input.context().promptVersion(), "1.0"));
        }
    }

    static class CostlyEchoAgent implements Agent<String, String> {
        @Override public String agentType() { return "costly_echo_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            return new AgentOutput<>(input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                    "echo:" + input.payload(), 0.95, List.of(), List.of(), false,
                    new AgentOutput.AgentMetadata("sonnet_gen_v1", "claude-sonnet-4-6", "anthropic",
                            1000, 500, 0.0180, 100, input.context().promptId(),
                            input.context().promptVersion(), "1.0"));
        }
    }

    static class FailingAgent implements Agent<String, String> {
        @Override public String agentType() { return "fail_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            throw new RuntimeException("Agent failure");
        }
    }

    // ─── Execution + output persistence ──────────────────────────────────

    @Test
    void executesAgentAndPersistsOutput() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentOutput<String> output = runner.run(new EchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "mock_v1");

        assertTrue(output.succeeded());
        assertEquals("echo:hello", output.output());

        // Two saves expected: RUNNING then SUCCEEDED
        verify(agentRunRepo, times(2)).save(any());

        // The second save must have output set (for idempotency cache).
        // Capture the argument passed to the second save call.
        ArgumentCaptor<CfAgentRun> captor = ArgumentCaptor.forClass(CfAgentRun.class);
        verify(agentRunRepo, times(2)).save(captor.capture());
        // After both saves, the run object's final state is SUCCEEDED with output persisted.
        // We verify by checking the output field is set (the same mutable object is saved twice).
        CfAgentRun finalState = captor.getAllValues().get(1);
        assertEquals("SUCCEEDED", finalState.getStatus());
        assertEquals("echo:hello", finalState.getOutput());
    }

    @Test
    void recordsCostOnRealExecution() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        runner.run(new CostlyEchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        verify(costLedger).record(eq(lessonId), eq(1), any(), eq("language"), eq("de"),
                eq("anthropic"), eq("claude-sonnet-4-6"), eq(1000), eq(500), eq(0.0180),
                argThat(k -> k.startsWith("per_lesson:")));
    }

    // ─── Idempotency cache ────────────────────────────────────────────────

    @Test
    void cacheHitReturnsPersistedOutputWithoutExecutingAgent() {
        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID())
                .lessonId(lessonId)
                .lessonVersion(1)
                .agentType("echo_agent")
                .status("SUCCEEDED")
                .output("echo:hello")        // persisted in prior execution
                .modelConfigKey("mock_v1")
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("echo_agent"), any()))
                .thenReturn(Optional.of(cached));

        AgentOutput<String> output = runner.run(new EchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "mock_v1");

        assertTrue(output.succeeded());
        assertEquals("echo:hello", output.output(), "Cache hit must return the persisted output");

        // Agent.execute must NOT be called — no new run record, no cost entry
        verify(agentRunRepo, never()).save(any());
        verifyNoInteractions(costLedger);
    }

    @Test
    void noDuplicateCostOnCacheHit() {
        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID()).lessonId(lessonId).lessonVersion(1)
                .agentType("costly_echo_agent").status("SUCCEEDED").output("echo:hello")
                .provider("anthropic").modelId("claude-sonnet-4-6")
                .inputTokens(1000).outputTokens(500)
                .estimatedCostUsd(java.math.BigDecimal.valueOf(0.0180))
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("costly_echo_agent"), any()))
                .thenReturn(Optional.of(cached));

        runner.run(new CostlyEchoAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        verifyNoInteractions(costLedger);
    }

    // ─── Failure handling ─────────────────────────────────────────────────

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
        CfAgentRun lastSave = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals("FAILED", lastSave.getStatus());
        assertNotNull(lastSave.getErrorMessage());
    }

    // ─── Canonical hashing ────────────────────────────────────────────────

    @Test
    void hashIsDeterministicForSameStringPayload() {
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

    @Test
    void hashIsCanonical_mapInsertionOrderDoesNotMatter() {
        // LinkedHashMap preserves insertion order; TreeMap sorts keys.
        // Both represent the same logical input — must produce the same hash.
        Map<String, Object> insertionOrdered = new LinkedHashMap<>();
        insertionOrdered.put("title", "Greetings");
        insertionOrdered.put("level", "A1");

        Map<String, Object> reverseOrdered = new LinkedHashMap<>();
        reverseOrdered.put("level", "A1");
        reverseOrdered.put("title", "Greetings");

        String h1 = AgentRunner.hash(lessonId, 1, "content_gen", insertionOrdered);
        String h2 = AgentRunner.hash(lessonId, 1, "content_gen", reverseOrdered);

        assertEquals(h1, h2, "Canonical hash must be order-independent for equivalent inputs");
    }

    @Test
    void hashDiffersForMateriallyDifferentMaps() {
        Map<String, Object> a = Map.of("title", "Greetings", "level", "A1");
        Map<String, Object> b = Map.of("title", "Farewells", "level", "A1");

        assertNotEquals(AgentRunner.hash(lessonId, 1, "content_gen", a),
                        AgentRunner.hash(lessonId, 1, "content_gen", b));
    }

    @Test
    void hashDiffersByLessonVersion() {
        String h1 = AgentRunner.hash(lessonId, 1, "agent", "input");
        String h2 = AgentRunner.hash(lessonId, 2, "agent", "input");
        assertNotEquals(h1, h2);
    }

    @Test
    void hashDiffersByAgentType() {
        String h1 = AgentRunner.hash(lessonId, 1, "agent-A", "input");
        String h2 = AgentRunner.hash(lessonId, 1, "agent-B", "input");
        assertNotEquals(h1, h2);
    }

    // ─── AgentOutput cache — Phase 0.2 correctness ───────────────────────

    /** Agent that returns issues, recommendations, and culturalFlag=true. */
    static class RichOutputAgent implements Agent<String, String> {
        @Override public String agentType() { return "rich_output_agent"; }
        @Override public AgentOutput<String> execute(AgentInput<String> input) {
            return new AgentOutput<>(
                    input.agentRunId(), AgentOutput.Status.SUCCEEDED,
                    "result:" + input.payload(), 0.88,
                    List.of(new Issue("CULTURAL_SENSITIVITY", Issue.Severity.WARNING, "body", "Check phrasing", null, true)),
                    List.of("Simplify vocabulary", "Add more examples"),
                    true,
                    new AgentOutput.AgentMetadata("sonnet_gen_v1", "claude-sonnet-4-6", "anthropic",
                            200, 100, 0.0050, 80,
                            input.context().promptId(), input.context().promptVersion(), "1.0")
            );
        }
    }

    @Test
    void persistsCompleteAgentOutputOnFirstExecution() {
        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), any(), any()))
                .thenReturn(Optional.empty());
        when(agentRunRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        runner.run(new RichOutputAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        ArgumentCaptor<CfAgentRun> captor = ArgumentCaptor.forClass(CfAgentRun.class);
        verify(agentRunRepo, times(2)).save(captor.capture());

        CfAgentRun saved = captor.getAllValues().get(1);
        assertEquals("SUCCEEDED", saved.getStatus());

        // issues persisted
        assertNotNull(saved.getIssues(), "issues must be persisted");
        List<?> savedIssues = (List<?>) saved.getIssues();
        assertEquals(1, savedIssues.size());

        // recommendations persisted
        assertNotNull(saved.getRecommendations(), "recommendations must be persisted");
        List<?> savedRecs = (List<?>) saved.getRecommendations();
        assertEquals(2, savedRecs.size());

        // culturalFlag persisted
        assertTrue(saved.isCulturalFlag(), "culturalFlag must be persisted as true");
    }

    @Test
    void cacheHitReconstructsCompleteAgentOutput() {
        // Simulate a run persisted by a prior execution — JSONB round-trip returns List<LinkedHashMap>
        // for Issue fields, matching what Hibernate deserialises from the database.
        java.util.LinkedHashMap<String, Object> issueMap = new java.util.LinkedHashMap<>();
        issueMap.put("code", "CULTURAL_SENSITIVITY");
        issueMap.put("severity", "WARNING");
        issueMap.put("field", "body");
        issueMap.put("message", "Check phrasing");
        issueMap.put("suggestion", null);
        issueMap.put("cultural", true);

        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID())
                .lessonId(lessonId).lessonVersion(1)
                .agentType("rich_output_agent").status("SUCCEEDED")
                .output("result:hello")
                .confidence(java.math.BigDecimal.valueOf(0.88))
                .issues(List.of(issueMap))
                .recommendations(List.of("Simplify vocabulary", "Add more examples"))
                .culturalFlag(true)
                .modelConfigKey("sonnet_gen_v1")
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("rich_output_agent"), any()))
                .thenReturn(Optional.of(cached));

        AgentOutput<String> output = runner.run(new RichOutputAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        assertTrue(output.succeeded());
        assertEquals("result:hello", output.output());

        // issues reconstructed as typed Issue objects
        assertEquals(1, output.issues().size());
        Issue issue = output.issues().get(0);
        assertEquals("CULTURAL_SENSITIVITY", issue.code());
        assertEquals(Issue.Severity.WARNING, issue.severity());
        assertEquals("body", issue.field());
        assertEquals("Check phrasing", issue.message());

        // recommendations reconstructed
        assertEquals(2, output.recommendations().size());
        assertEquals("Simplify vocabulary", output.recommendations().get(0));
        assertEquals("Add more examples", output.recommendations().get(1));

        // culturalFlag reconstructed
        assertTrue(output.culturalFlag(), "culturalFlag must be true on cache hit");
    }

    @Test
    void agentNotExecutedOnCacheHit() {
        CfAgentRun cached = CfAgentRun.builder()
                .id(UUID.randomUUID())
                .lessonId(lessonId).lessonVersion(1)
                .agentType("rich_output_agent").status("SUCCEEDED")
                .output("result:hello")
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("rich_output_agent"), any()))
                .thenReturn(Optional.of(cached));

        // Track whether execute() was called by wrapping in a spy-like subclass.
        boolean[] executed = {false};
        Agent<String, String> spyAgent = new RichOutputAgent() {
            @Override public AgentOutput<String> execute(AgentInput<String> input) {
                executed[0] = true;
                return super.execute(input);
            }
        };

        runner.run(spyAgent, lessonId, 1, "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        assertFalse(executed[0], "Agent.execute must NOT be called on a cache hit");
        // No new run record created
        verify(agentRunRepo, never()).save(any());
    }

    @Test
    void noDuplicateRunOnCacheHit() {
        UUID existingRunId = UUID.randomUUID();
        CfAgentRun cached = CfAgentRun.builder()
                .id(existingRunId)
                .lessonId(lessonId).lessonVersion(1)
                .agentType("rich_output_agent").status("SUCCEEDED")
                .output("result:hello")
                .build();

        when(agentRunRepo.findSucceededByIdempotencyKey(any(), anyInt(), eq("rich_output_agent"), any()))
                .thenReturn(Optional.of(cached));

        AgentOutput<String> output = runner.run(new RichOutputAgent(), lessonId, 1,
                "language", "de", "hello", promptId, 1, "sonnet_gen_v1");

        // The returned run ID must be the existing run ID — no new row created.
        assertEquals(existingRunId, output.agentRunId(),
                "Cache hit must return the existing run ID, not create a new run");
        verify(agentRunRepo, never()).save(any());
    }
}
