package com.dsalearner.pipeline.language.german;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.language.LessonContent;
import com.dsalearner.pipeline.language.RevisionGenerationInput;
import com.dsalearner.pipeline.provider.LlmProviderException;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import com.dsalearner.pipeline.provider.LlmRequest;
import com.dsalearner.pipeline.provider.LlmResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Revises a German A1 lesson based on QA feedback.
 *
 * Prompt template variables:
 *   {{lessonJson}}        — the original lesson JSON (from the failing version)
 *   {{revisionFeedback}}  — structured QA issues as JSON string
 *
 * Architecture is identical to GermanA1ContentGenerationAgent — same parsing,
 * same output type — only the prompt key and template variables differ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GermanA1RevisionGenerationAgent implements Agent<RevisionGenerationInput, LessonContent> {

    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final LlmProviderRegistry providerRegistry;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    @Override
    public String agentType() { return AgentType.REVISION_GENERATOR; }

    @Override
    public AgentOutput<LessonContent> execute(AgentInput<RevisionGenerationInput> input) {
        RevisionGenerationInput payload = input.payload();
        AgentInput.AgentContext ctx = input.context();

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve("language.german.a1.revision_generation");
        ModelConfig modelConfig = modelRouter.resolve(agentType(), ctx.domainCode(), ctx.languageCode());

        String userPrompt = prompt.promptText()
                .replace("{{lessonJson}}", payload.lessonJson())
                .replace("{{revisionFeedback}}", payload.revisionFeedback());

        log.info("GermanA1RevisionAgent: revising lessonId={} sourceVersion={} model={}",
                input.lessonId(), payload.sourceVersion(), modelConfig.modelId());

        LlmResponse llmResponse;
        try {
            llmResponse = providerRegistry.get(modelConfig.provider()).generate(
                    new LlmRequest(
                            modelConfig.modelId(),
                            prompt.systemPrompt(),
                            userPrompt,
                            modelConfig.maxTokens(),
                            modelConfig.temperature(),
                            modelConfig.timeoutMs()
                    )
            );
        } catch (LlmProviderException e) {
            throw e;
        }

        LessonContent lessonContent = parseResponse(llmResponse.text(), input.lessonId());
        double estimatedCost = modelConfig.estimateCost(llmResponse.inputTokens(), llmResponse.outputTokens());

        return new AgentOutput<>(
                input.agentRunId(),
                AgentOutput.Status.SUCCEEDED,
                lessonContent,
                0.95,
                List.of(),
                List.of(),
                false,
                new AgentOutput.AgentMetadata(
                        modelConfig.configKey(),
                        llmResponse.modelId(),
                        llmResponse.provider(),
                        llmResponse.inputTokens(),
                        llmResponse.outputTokens(),
                        estimatedCost,
                        0,
                        prompt.id(),
                        prompt.version(),
                        "1.0"
                )
        );
    }

    @SuppressWarnings("unchecked")
    private LessonContent parseResponse(String raw, java.util.UUID lessonId) {
        String json = extractJson(raw);
        try {
            Map<String, Object> root = MAPPER.readValue(json, MAP_TYPE);

            Map<String, Object> metadata    = (Map<String, Object>) root.get("metadata");
            List<String>        objectives  = (List<String>) root.get("objectives");
            Map<String, Object> explanation = (Map<String, Object>) root.get("explanation");
            List<Map<String, Object>> vocab = (List<Map<String, Object>>) root.get("vocabulary");
            Map<String, Object> grammar     = (Map<String, Object>) root.get("grammar");
            List<Map<String, Object>> examp = (List<Map<String, Object>>) root.get("examples");
            List<Map<String, Object>> exers = (List<Map<String, Object>>) root.get("exercises");

            validateNotNull(metadata,    "metadata",    lessonId);
            validateNotNull(objectives,  "objectives",  lessonId);
            validateNotNull(explanation, "explanation", lessonId);
            validateNotNull(vocab,       "vocabulary",  lessonId);
            validateNotNull(grammar,     "grammar",     lessonId);
            validateNotNull(examp,       "examples",    lessonId);
            validateNotNull(exers,       "exercises",   lessonId);

            return new LessonContent(metadata, objectives, explanation, vocab, grammar, examp, exers);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "GermanA1RevisionAgent: failed to parse model response for lessonId=" + lessonId
                    + ". Response snippet: " + raw.substring(0, Math.min(200, raw.length())), e);
        }
    }

    private void validateNotNull(Object field, String name, java.util.UUID lessonId) {
        if (field == null) {
            throw new IllegalStateException(
                    "GermanA1RevisionAgent: model response missing required field '" + name
                    + "' for lessonId=" + lessonId);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }
}
