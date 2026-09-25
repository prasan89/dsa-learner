package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.fasterxml.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY;

/**
 * Executes agents with idempotency, run tracking, and cost recording.
 *
 * Idempotency: if an identical (lessonId, lessonVersion, agentType, inputHash)
 * already has status=SUCCEEDED, returns the cached output without calling the agent.
 *
 * Canonical hashing: payload is serialized via Jackson with sorted keys so that
 * equivalent structured inputs (e.g. Maps with different insertion order) produce
 * the same SHA-256 hash. Hashing failure throws — never falls back to random.
 *
 * Complete output contract: on success, the full AgentOutput — including issues,
 * recommendations, and culturalFlag — is persisted to cf_agent_runs so that cache
 * hits reconstruct an equivalent AgentOutput without re-executing the agent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgentRunner {

    private final CfAgentRunRepository agentRunRepository;
    private final CostLedgerService costLedgerService;

    // Canonical JSON serializer: sorted keys, no indentation.
    private static final ObjectMapper CANONICAL_MAPPER = new ObjectMapper()
            .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private static final TypeReference<List<Issue>>   ISSUE_LIST_TYPE   = new TypeReference<>() {};
    private static final TypeReference<List<String>>  STRING_LIST_TYPE  = new TypeReference<>() {};

    public <TInput, TOutput> AgentOutput<TOutput> run(
            Agent<TInput, TOutput> agent,
            UUID lessonId,
            int lessonVersion,
            String domainCode,
            String languageCode,
            TInput payload,
            UUID promptId,
            int promptVersion,
            String modelConfigKey) {

        String inputHash = hash(lessonId, lessonVersion, agent.agentType(), payload);

        // ─── Idempotency check ────────────────────────────────────────────
        Optional<CfAgentRun> existing = agentRunRepository
                .findSucceededByIdempotencyKey(lessonId, lessonVersion, agent.agentType(), inputHash);

        if (existing.isPresent()) {
            log.info("AgentRunner: cache hit for lessonId={} version={} agent={} hash={}",
                    lessonId, lessonVersion, agent.agentType(), inputHash);
            return buildCachedOutput(existing.get());
        }

        // ─── Create run record ────────────────────────────────────────────
        UUID runId = UUID.randomUUID();
        CfAgentRun run = CfAgentRun.builder()
                .id(runId)
                .lessonId(lessonId)
                .lessonVersion(lessonVersion)
                .agentType(agent.agentType())
                .domainCode(domainCode)
                .languageCode(languageCode)
                .status("RUNNING")
                .inputHash(inputHash)
                .promptId(promptId)
                .promptVersion(promptVersion)
                .modelConfigKey(modelConfigKey)
                .startedAt(Instant.now())
                .build();
        agentRunRepository.save(run);

        // ─── Build input ──────────────────────────────────────────────────
        AgentInput<TInput> input = new AgentInput<>(
                runId, lessonId, lessonVersion, inputHash, payload,
                new AgentInput.AgentContext(
                        domainCode, languageCode, null, null,
                        0, 3,
                        promptId, promptVersion, modelConfigKey
                )
        );

        // ─── Execute ──────────────────────────────────────────────────────
        AgentOutput<TOutput> output;
        try {
            output = agent.execute(input);
        } catch (Exception e) {
            log.error("Agent {} failed for lesson {}: {}", agent.agentType(), lessonId, e.getMessage());
            run.setStatus("FAILED");
            run.setErrorMessage(e.getMessage());
            run.setCompletedAt(Instant.now());
            agentRunRepository.save(run);
            throw e;
        }

        // ─── Persist complete output (enables idempotency cache reconstruction) ─
        String status = output.succeeded() ? "SUCCEEDED" : "FAILED";
        run.setStatus(status);
        run.setCompletedAt(Instant.now());
        run.setLatencyMs(java.time.Duration.between(run.getStartedAt(), run.getCompletedAt()).toMillis());

        run.setOutput(output.output());
        run.setConfidence(output.confidence() > 0
                ? new java.math.BigDecimal(String.valueOf(output.confidence())) : null);

        // Persist issues, recommendations, culturalFlag so cache hits return the complete contract.
        run.setIssues(output.issues());
        run.setRecommendations(output.recommendations());
        run.setCulturalFlag(output.culturalFlag());

        if (output.metadata() != null) {
            AgentOutput.AgentMetadata m = output.metadata();
            run.setInputTokens(m.inputTokens());
            run.setOutputTokens(m.outputTokens());
            run.setEstimatedCostUsd(new java.math.BigDecimal(String.valueOf(m.estimatedCostUsd())));
            run.setProvider(m.provider());
            run.setModelId(m.modelId());
        }
        agentRunRepository.save(run);

        // ─── Record cost (only on success; cache hits never reach here) ───
        if (output.succeeded() && output.metadata() != null && output.metadata().estimatedCostUsd() > 0) {
            AgentOutput.AgentMetadata m = output.metadata();
            costLedgerService.record(lessonId, lessonVersion, runId,
                    domainCode, languageCode,
                    m.provider(), m.modelId(),
                    m.inputTokens(), m.outputTokens(), m.estimatedCostUsd(),
                    "per_lesson:" + lessonId);
        }

        log.info("AgentRunner: {} completed status={} lessonId={} version={}",
                agent.agentType(), status, lessonId, lessonVersion);
        return output;
    }

    /**
     * Reconstructs a complete AgentOutput from a persisted SUCCEEDED run.
     *
     * issues and recommendations are deserialized from JSONB. If either field is
     * null (legacy run before Phase 0.2) an empty list is returned rather than
     * throwing, keeping backwards compatibility with existing data.
     *
     * If deserialization of persisted data produces an unexpected type, an
     * IllegalStateException is thrown rather than silently returning incomplete data.
     */
    @SuppressWarnings("unchecked")
    private <TOutput> AgentOutput<TOutput> buildCachedOutput(CfAgentRun run) {
        List<Issue> issues = deserializeIssues(run);
        List<String> recommendations = deserializeRecommendations(run);

        return new AgentOutput<>(
                run.getId(),
                AgentOutput.Status.SUCCEEDED,
                (TOutput) run.getOutput(),
                run.getConfidence() != null ? run.getConfidence().doubleValue() : 1.0,
                issues,
                recommendations,
                run.isCulturalFlag(),
                new AgentOutput.AgentMetadata(
                        run.getModelConfigKey(), run.getModelId(), run.getProvider(),
                        run.getInputTokens() != null ? run.getInputTokens() : 0,
                        run.getOutputTokens() != null ? run.getOutputTokens() : 0,
                        run.getEstimatedCostUsd() != null ? run.getEstimatedCostUsd().doubleValue() : 0.0,
                        run.getLatencyMs() != null ? run.getLatencyMs() : 0,
                        run.getPromptId(), run.getPromptVersion() != null ? run.getPromptVersion() : 0,
                        "cached"
                )
        );
    }

    private List<Issue> deserializeIssues(CfAgentRun run) {
        Object raw = run.getIssues();
        if (raw == null) return List.of();
        if (raw instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof Issue)) {
            return (List<Issue>) raw;
        }
        try {
            return CANONICAL_MAPPER.convertValue(raw, ISSUE_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: failed to deserialize issues for run " + run.getId(), e);
        }
    }

    private List<String> deserializeRecommendations(CfAgentRun run) {
        Object raw = run.getRecommendations();
        if (raw == null) return List.of();
        if (raw instanceof List<?> list && (list.isEmpty() || list.get(0) instanceof String)) {
            return (List<String>) raw;
        }
        try {
            return CANONICAL_MAPPER.convertValue(raw, STRING_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: failed to deserialize recommendations for run " + run.getId(), e);
        }
    }

    /**
     * Produces a SHA-256 idempotency hash for the given execution identity.
     *
     * Payload serialization uses canonical JSON (sorted keys) so that equivalent
     * structured inputs — e.g. Maps with different insertion order — always produce
     * the same hash. Primitives and Strings are serialized as JSON scalars.
     *
     * Throws IllegalStateException on serialization or digest failure rather than
     * silently falling back to random — idempotency must be correct or fail fast.
     */
    static String hash(UUID lessonId, int version, String agentType, Object payload) {
        try {
            String serializedPayload = CANONICAL_MAPPER.writeValueAsString(payload);
            String data = lessonId + ":" + version + ":" + agentType + ":" + serializedPayload;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: canonical hash failed for lessonId=%s agent=%s — aborting to preserve idempotency"
                            .formatted(lessonId, agentType), e);
        }
    }
}
