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
 * Calls the LLM to generate a per-level curriculum blueprint.
 * Input carries the language profile + CEFR level + optional prior-level context.
 * Output is a LevelBlueprint parsed from JSON.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CurriculumBlueprintAgent implements Agent<BlueprintInput, LevelBlueprint> {

    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final LlmProviderRegistry providerRegistry;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String agentType() {
        return AgentType.CURRICULUM_BLUEPRINT_GENERATOR;
    }

    @Override
    public AgentOutput<LevelBlueprint> execute(AgentInput<BlueprintInput> input) {
        BlueprintInput payload = input.payload();
        AgentInput.AgentContext ctx = input.context();

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(agentType());
        ModelConfig modelConfig = modelRouter.resolve(agentType(), ctx.domainCode(), ctx.languageCode());

        String userPrompt = prompt.promptText()
                .replace("{{languageCode}}", payload.languageCode())
                .replace("{{languageDisplayName}}", payload.languageDisplayName())
                .replace("{{script}}", payload.script() != null ? payload.script() : "Latin")
                .replace("{{domainCode}}", payload.domainCode())
                .replace("{{cefrLevels}}", String.join(", ", payload.cefrLevels()))
                .replace("{{curriculumGoals}}", payload.curriculumGoals() != null ? payload.curriculumGoals() : "")
                .replace("{{proficiencyFramework}}", payload.proficiencyFramework() != null
                        ? payload.proficiencyFramework() : "CEFR");

        log.info("{}: generating blueprint for {} {} levels=[{}]",
                agentType(), payload.languageDisplayName(), payload.languageCode(),
                String.join(",", payload.cefrLevels()));

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

        LevelBlueprint result = parseBlueprint(llmResponse.text(), payload);
        double cost = modelConfig.estimateCost(llmResponse.inputTokens(), llmResponse.outputTokens());

        return new AgentOutput<>(
                input.agentRunId(),
                AgentOutput.Status.SUCCEEDED,
                result,
                0.95,
                List.of(),
                List.of(),
                false,
                new AgentOutput.AgentMetadata(
                        modelConfig.configKey(), modelConfig.modelId(), modelConfig.provider(),
                        llmResponse.inputTokens(), llmResponse.outputTokens(), cost,
                        0L, prompt.id(), prompt.version(), "live"
                )
        );
    }

    private LevelBlueprint parseBlueprint(String text, BlueprintInput payload) {
        try {
            String json = extractJson(text);
            return MAPPER.readValue(json, LevelBlueprint.class);
        } catch (Exception e) {
            log.error("{}: failed to parse blueprint JSON — {}",
                    agentType(), e.getMessage());
            // Return a minimal blueprint so the orchestrator can record the failure
            return new LevelBlueprint(
                    payload.cefrLevels().isEmpty() ? "A1" : payload.cefrLevels().get(0),
                    payload.languageDisplayName() + " " +
                            (payload.cefrLevels().isEmpty() ? "A1" : payload.cefrLevels().get(0)),
                    "Parse failed: " + e.getMessage(),
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
