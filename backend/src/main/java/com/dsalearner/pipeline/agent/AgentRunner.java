package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
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
            CfAgentRun cached = existing.get();
            return buildCachedOutput(cached);
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

        // ─── Update run record (persist output for idempotency cache) ─────
        String status = output.succeeded() ? "SUCCEEDED" : "FAILED";
        run.setStatus(status);
        run.setCompletedAt(Instant.now());
        run.setLatencyMs(java.time.Duration.between(run.getStartedAt(), run.getCompletedAt()).toMillis());

        // Persist the output so cache hits can reconstruct the result without re-executing.
        run.setOutput(output.output());
        run.setConfidence(output.confidence() > 0
                ? new java.math.BigDecimal(String.valueOf(output.confidence())) : null);

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

    @SuppressWarnings("unchecked")
    private <TOutput> AgentOutput<TOutput> buildCachedOutput(CfAgentRun run) {
        return new AgentOutput<>(
                run.getId(),
                AgentOutput.Status.SUCCEEDED,
                (TOutput) run.getOutput(),
                run.getConfidence() != null ? run.getConfidence().doubleValue() : 1.0,
                List.of(),
                List.of(),
                false,
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
