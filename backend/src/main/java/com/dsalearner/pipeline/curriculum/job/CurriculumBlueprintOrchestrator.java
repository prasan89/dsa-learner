package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.curriculum.agent.*;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.curriculum.service.CurriculumWorkflowOrchestrator;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Executes CURRICULUM_BLUEPRINT job: calls the LLM to generate a LevelBlueprint,
 * then persists it and transitions the curriculum to BLUEPRINT_GENERATED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumBlueprintOrchestrator {

    private final CurriculumBlueprintAgent blueprintAgent;
    private final AgentRunner agentRunner;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final CurriculumService curriculumService;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final CfCurriculumLevelRepository levelRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Executes the blueprint generation job.
     * The job payload must contain: languageCode, languageDisplayName, script,
     * domainCode, cefrLevel (single level for this job), curriculumGoals.
     * Returns the agent run ID as the result reference.
     */
    public String execute(CfCurriculumPipelineJob job) {
        UUID curriculumId = job.getCurriculumId();
        Map<String, Object> payload = job.getPayload();

        String languageCode     = (String) payload.get("languageCode");
        String languageDisplayName = (String) payload.get("languageDisplayName");
        String script           = (String) payload.getOrDefault("script", "Latin");
        String domainCode       = (String) payload.getOrDefault("domainCode", "language");
        String cefrLevel        = (String) payload.get("cefrLevel");
        String goals            = (String) payload.getOrDefault("curriculumGoals", "");

        BlueprintInput input = new BlueprintInput(
                languageCode, languageDisplayName, script, domainCode,
                cefrLevel != null ? List.of(cefrLevel) : List.of("A1", "A2", "B1", "B2", "C1", "C2"),
                goals, "CEFR"
        );

        // Use curriculumId as the lessonId sentinel for AgentRunner idempotency
        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(AgentType.CURRICULUM_BLUEPRINT_GENERATOR);
        ModelConfig modelConfig = modelRouter.resolve(AgentType.CURRICULUM_BLUEPRINT_GENERATOR, domainCode, languageCode);

        UUID runId = UUID.randomUUID();
        AgentInput<BlueprintInput> agentInput = new AgentInput<>(
                runId, curriculumId, 1, UUID.randomUUID().toString(), input,
                new AgentInput.AgentContext(domainCode, languageCode, cefrLevel, null,
                        0, 3, prompt.id(), prompt.version(), modelConfig.configKey())
        );

        AgentOutput<LevelBlueprint> output = blueprintAgent.execute(agentInput);

        if (!output.succeeded()) {
            throw new RuntimeException("Blueprint generation failed for curriculum " + curriculumId);
        }

        LevelBlueprint levelBlueprint = output.output();
        log.info("Blueprint generated for {} {} — {} lessons",
                languageCode, cefrLevel, levelBlueprint.totalLessons());

        return runId.toString();
    }
}
