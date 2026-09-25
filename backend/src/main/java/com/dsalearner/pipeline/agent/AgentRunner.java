package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.service.CostLedgerService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import java.util.Map;
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
    // FAIL_ON_UNKNOWN_PROPERTIES disabled so cached runs written before a schema change
    // (e.g. Issue gaining the 'evidence' field and losing 'error') still deserialize.
    private static final ObjectMapper CANONICAL_MAPPER = new ObjectMapper()
            .configure(SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
        // Capture the returned entity: Spring Data calls merge() for entities with a pre-set ID,
        // and merge() returns a NEW managed instance with @CreationTimestamp populated. The
        // original `run` variable is an unmanaged copy whose createdAt field stays null — any
        // subsequent save() on the original would send created_at=null to PostgreSQL.
        run = agentRunRepository.save(run);

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

        // Convert to a plain Map so Hibernate's JacksonJsonFormatMapper can serialize it to JSONB.
        // Java records with ImmutableCollections fields cause a ClassCastException inside
        // JacksonJsonFormatMapper.toString() when the field type is Object — the mapper tries
        // to treat the value as a pre-serialized String. Converting via ObjectMapper first
        // produces a Map<String,Object> that Hibernate can serialize without issue.
        run.setOutput(toSerializableMap(output.output()));
        run.setConfidence(output.confidence() > 0
                ? new java.math.BigDecimal(String.valueOf(output.confidence())) : null);

        // Persist issues, recommendations, culturalFlag so cache hits return the complete contract.
        // Use new ArrayList<> — CfAgentRun.issues/recommendations are List<Object>; the unchecked
        // cast is safe here because we only ever write Issue/String elements and read them back
        // through the typed deserialize methods.
        @SuppressWarnings("unchecked")
        List<Object> issueList = output.issues() != null
                ? new java.util.ArrayList<>((List<Object>)(List<?>)output.issues()) : null;
        @SuppressWarnings("unchecked")
        List<Object> recList = output.recommendations() != null
                ? new java.util.ArrayList<>((List<Object>)(List<?>)output.recommendations()) : null;
        run.setIssues(issueList);
        run.setRecommendations(recList);
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

    @SuppressWarnings("unchecked")
    private List<Issue> deserializeIssues(CfAgentRun run) {
        List<Object> raw = run.getIssues();
        if (raw == null) return List.of();
        if (raw.isEmpty() || raw.get(0) instanceof Issue) {
            return (List<Issue>)(List<?>)raw;
        }
        try {
            return CANONICAL_MAPPER.convertValue(raw, ISSUE_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: failed to deserialize issues for run " + run.getId(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> deserializeRecommendations(CfAgentRun run) {
        List<Object> raw = run.getRecommendations();
        if (raw == null) return List.of();
        if (raw.isEmpty() || raw.get(0) instanceof String) {
            return (List<String>)(List<?>)raw;
        }
        try {
            return CANONICAL_MAPPER.convertValue(raw, STRING_LIST_TYPE);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: failed to deserialize recommendations for run " + run.getId(), e);
        }
    }

    /**
     * Converts a Java record or complex object to a plain Map<String,Object> for safe JSONB
     * serialization. Hibernate's JacksonJsonFormatMapper fails to serialize Java records whose
     * fields contain ImmutableCollections because it tries to cast the list value to String when
     * the entity field type is Object. Converting via ObjectMapper round-trip produces a plain
     * Map<String,Object> with standard mutable collections that Hibernate handles without issue.
     * Converts an arbitrary Java object to a plain Map<String,Object> for JSONB serialization.
     *
     * Hibernate 6.5.2's JacksonJsonFormatMapper.toString(value, javaType) casts to String
     * when javaType is either String.class OR Object.class (see bytecode at line 23 of
     * the checkcast instruction). Storing anything other than a String in an Object-typed
     * JSONB field therefore always fails with ClassCastException.
     *
     * Changing the entity field type to Map<String,Object> avoids this, but requires the
     * value passed to setOutput() to always be a Map. This method guarantees that:
     * - null → null
     * - Map → returned directly (already correct type)
     * - String/primitive → wrapped as {"value": "<scalar>"} for DB; callers check "value" key
     * - Java record or POJO → Jackson round-trip to Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toSerializableMap(Object value) {
        if (value == null) return null;
        if (value instanceof Map<?,?> m) {
            return (Map<String, Object>) m;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return Map.of("value", value);
        }
        try {
            String json = CANONICAL_MAPPER.writeValueAsString(value);
            if (!json.startsWith("{")) {
                return Map.of("value", value);
            }
            return CANONICAL_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalStateException(
                    "AgentRunner: failed to convert output to serializable map", e);
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
