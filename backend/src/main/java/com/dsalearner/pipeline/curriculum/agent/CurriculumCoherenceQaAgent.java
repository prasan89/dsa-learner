package com.dsalearner.pipeline.curriculum.agent;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.provider.LlmProviderRegistry;
import com.dsalearner.pipeline.provider.LlmRequest;
import com.dsalearner.pipeline.provider.LlmResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Cross-curriculum coherence QA — checks grammar/vocab progression across all levels.
 * Input is a CurriculumQaInput with curriculumSummaryJson covering all levels.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumCoherenceQaAgent implements Agent<CurriculumQaInput, CurriculumQaResult> {

    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final LlmProviderRegistry providerRegistry;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String agentType() {
        return AgentType.CURRICULUM_COHERENCE_QA;
    }

    @Override
    public AgentOutput<CurriculumQaResult> execute(AgentInput<CurriculumQaInput> input) {
        CurriculumQaInput payload = input.payload();
        AgentInput.AgentContext ctx = input.context();

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(agentType());
        ModelConfig modelConfig = modelRouter.resolve(agentType(), ctx.domainCode(), ctx.languageCode());

        String userPrompt = prompt.promptText()
                .replace("{{languageCode}}", payload.languageCode())
                .replace("{{languageDisplayName}}", payload.languageDisplayName())
                .replace("{{deterministicCheckSummary}}", payload.deterministicCheckSummary() != null
                        ? payload.deterministicCheckSummary() : "")
                .replace("{{curriculumSummaryJson}}", payload.curriculumSummaryJson() != null
                        ? payload.curriculumSummaryJson() : "{}");

        log.info("{}: coherence check for {} full curriculum", agentType(), payload.languageDisplayName());

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

        CurriculumQaResult result = parseResult(llmResponse.text());
        double cost = modelConfig.estimateCost(llmResponse.inputTokens(), llmResponse.outputTokens());
        boolean passed = result.passed();

        return new AgentOutput<>(
                input.agentRunId(),
                passed ? AgentOutput.Status.SUCCEEDED : AgentOutput.Status.PARTIAL,
                result,
                passed ? 0.9 : 0.5,
                List.of(),
                result.recommendations() != null ? result.recommendations() : List.of(),
                false,
                new AgentOutput.AgentMetadata(
                        modelConfig.configKey(), modelConfig.modelId(), modelConfig.provider(),
                        llmResponse.inputTokens(), llmResponse.outputTokens(), cost,
                        0L, prompt.id(), prompt.version(), "live"
                )
        );
    }

    private CurriculumQaResult parseResult(String text) {
        try {
            String json = extractJson(text);
            return MAPPER.readValue(json, CurriculumQaResult.class);
        } catch (Exception e) {
            log.error("{}: failed to parse coherence QA JSON — {}", agentType(), e.getMessage());
            return new CurriculumQaResult(
                    "Parse failure: " + e.getMessage(), "FAILED",
                    List.of(new CurriculumQaIssue("ERROR", null, null, null, 0,
                            null, "response", "Failed to parse LLM response", null)),
                    List.of()
            );
        }
    }

    private String extractJson(String text) {
        String t = text.strip();
        if (t.startsWith("```")) {
            int firstNewline = t.indexOf('\n');
            if (firstNewline > 0) t = t.substring(firstNewline + 1).strip();
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3).strip();
        }
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) return t.substring(start, end + 1);
        return t;
    }
}
