package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import com.dsalearner.pipeline.provider.LlmRequest;
import com.dsalearner.pipeline.provider.LlmResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Base class for all German A1 QA agents.
 *
 * Each subclass supplies:
 *  - agentType()     — from AgentType constants
 *  - promptKey()     — the prompt registry key for this agent's prompt
 *
 * The shared logic:
 *  1. Resolve prompt + model from registry (not hard-coded).
 *  2. Inject the lesson JSON into the prompt template.
 *  3. Call the LLM provider.
 *  4. Parse the JSON response into a QaResult.
 *  5. Return a fully-populated AgentOutput.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseGermanQaAgent implements Agent<QaInput, QaResult> {

    protected final PromptRegistry promptRegistry;
    protected final ModelRouter modelRouter;
    protected final LlmProviderRegistry providerRegistry;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    protected abstract String promptKey();

    @Override
    public AgentOutput<QaResult> execute(AgentInput<QaInput> input) {
        QaInput payload = input.payload();
        AgentInput.AgentContext ctx = input.context();

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(promptKey());
        ModelConfig modelConfig = modelRouter.resolve(agentType(), ctx.domainCode(), ctx.languageCode());

        String userPrompt = prompt.promptText().replace("{{lessonJson}}", payload.lessonJson());

        log.info("{}: evaluating lessonId={} model={}", agentType(), input.lessonId(), modelConfig.modelId());

        LlmResponse llmResponse = providerRegistry.get(modelConfig.provider()).generate(
                new LlmRequest(
                        modelConfig.modelId(),
                        prompt.systemPrompt(),
                        userPrompt,
                        modelConfig.maxTokens(),
                        modelConfig.temperature(),
                        modelConfig.timeoutMs()
                )
        );

        QaResult result = parseResponse(llmResponse.text(), input.lessonId());
        double estimatedCost = modelConfig.estimateCost(llmResponse.inputTokens(), llmResponse.outputTokens());

        boolean hasErrors = result.hasErrors();
        AgentOutput.Status status = hasErrors ? AgentOutput.Status.PARTIAL : AgentOutput.Status.SUCCEEDED;

        return new AgentOutput<>(
                input.agentRunId(),
                status,
                result,
                hasErrors ? 0.6 : 0.95,
                result.issues() != null ? result.issues() : List.of(),
                result.recommendations() != null ? result.recommendations() : List.of(),
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
    private QaResult parseResponse(String raw, UUID lessonId) {
        String json = extractJson(raw);
        try {
            Map<String, Object> root = MAPPER.readValue(json, MAP_TYPE);

            String overallAssessment = (String) root.getOrDefault("overallAssessment", "");

            List<Map<String, Object>> rawIssues = (List<Map<String, Object>>) root.getOrDefault("issues", List.of());
            List<Issue> issues = new ArrayList<>();
            for (Map<String, Object> rawIssue : rawIssues) {
                String severityStr = (String) rawIssue.getOrDefault("severity", "INFO");
                Issue.Severity severity;
                try {
                    severity = Issue.Severity.valueOf(severityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    severity = Issue.Severity.INFO;
                }
                issues.add(new Issue(
                        agentType() + "_issue",
                        severity,
                        (String) rawIssue.getOrDefault("field", ""),
                        (String) rawIssue.getOrDefault("message", ""),
                        (String) rawIssue.getOrDefault("suggestion", null),
                        false,
                        (String) rawIssue.getOrDefault("evidence", null)
                ));
            }

            List<String> recommendations = (List<String>) root.getOrDefault("recommendations", List.of());

            return new QaResult(overallAssessment, issues, new ArrayList<>(recommendations));

        } catch (Exception e) {
            throw new IllegalStateException(
                    agentType() + ": failed to parse QA response for lessonId=" + lessonId
                    + ". Snippet: " + raw.substring(0, Math.min(200, raw.length())), e);
        }
    }

    private String extractJson(String text) {
        String t = text.strip();
        // Strip markdown code fences: ```json\n...\n``` or ```\n...\n```
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1).strip();
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3).strip();
        }
        int start = t.indexOf('{');
        int end   = t.lastIndexOf('}');
        if (start >= 0 && end > start) return t.substring(start, end + 1);
        return t;
    }
}
