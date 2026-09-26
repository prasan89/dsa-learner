package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.curriculum.agent.*;
import com.dsalearner.pipeline.curriculum.service.CurriculumService;
import com.dsalearner.pipeline.curriculum.service.CurriculumWorkflowOrchestrator;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumPipelineJobRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Executes CURRICULUM_BLUEPRINT jobs one CEFR level at a time.
 *
 * Each invocation generates one LevelBlueprint, serialises it into the job's
 * resultReference field, then checks whether all 6 level jobs for this curriculum
 * have succeeded.  The last job to succeed assembles the full CurriculumBlueprint,
 * persists it (units + lesson-plan rows), and advances the curriculum status through
 * BLUEPRINT_GENERATED → BLUEPRINT_VALIDATED → GENERATION_IN_PROGRESS.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumBlueprintOrchestrator {

    private static final int EXPECTED_LEVELS = 6;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CurriculumBlueprintAgent blueprintAgent;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final CurriculumService curriculumService;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final CfCurriculumPipelineJobRepository jobRepository;

    /**
     * Generate one LevelBlueprint and, if it is the last one, assemble + persist.
     * Returns a JSON string of the generated LevelBlueprint as the resultReference.
     */
    public String execute(CfCurriculumPipelineJob job) {
        UUID curriculumId = job.getCurriculumId();
        Map<String, Object> payload = job.getPayload();

        String languageCode        = (String) payload.get("languageCode");
        String languageDisplayName = (String) payload.getOrDefault("languageDisplayName", languageCode);
        String script              = (String) payload.getOrDefault("script", "Latin");
        String domainCode          = (String) payload.getOrDefault("domainCode", "language");
        String cefrLevel           = (String) payload.get("cefrLevel");
        String goals               = (String) payload.getOrDefault("curriculumGoals", "");

        if (cefrLevel == null || cefrLevel.isBlank()) {
            throw new IllegalArgumentException("CURRICULUM_BLUEPRINT job missing cefrLevel in payload");
        }

        BlueprintInput input = new BlueprintInput(
                languageCode, languageDisplayName, script, domainCode,
                List.of(cefrLevel), goals, "CEFR"
        );

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(AgentType.CURRICULUM_BLUEPRINT_GENERATOR);
        ModelConfig modelConfig = modelRouter.resolve(AgentType.CURRICULUM_BLUEPRINT_GENERATOR, domainCode, languageCode);

        UUID runId = UUID.randomUUID();
        AgentInput<BlueprintInput> agentInput = new AgentInput<>(
                runId, curriculumId, 1, UUID.randomUUID().toString(), input,
                new AgentInput.AgentContext(domainCode, languageCode, cefrLevel, null,
                        0, 3, prompt.id(), prompt.version(), modelConfig.configKey())
        );

        log.info("CURRICULUM_BLUEPRINT: generating {} blueprint for curriculum={} language={}",
                cefrLevel, curriculumId, languageDisplayName);

        AgentOutput<CurriculumBlueprint> output = blueprintAgent.execute(agentInput);

        if (!output.succeeded()) {
            throw new RuntimeException("Blueprint generation failed for " + cefrLevel + " curriculum=" + curriculumId);
        }

        CurriculumBlueprint levelResponse = output.output();

        if (levelResponse.levels() == null || levelResponse.levels().isEmpty()) {
            throw new RuntimeException("Blueprint for " + cefrLevel + " produced no levels — LLM output malformed");
        }

        LevelBlueprint levelBlueprint = levelResponse.levels().get(0);
        log.info("CURRICULUM_BLUEPRINT: {} parsed — {} units, {} lessons",
                cefrLevel, levelBlueprint.units() != null ? levelBlueprint.units().size() : 0,
                levelBlueprint.totalLessons());

        // Serialise this level's blueprint for storage in resultReference
        String levelBlueprintJson;
        try {
            levelBlueprintJson = MAPPER.writeValueAsString(levelBlueprint);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise LevelBlueprint for " + cefrLevel, e);
        }

        // The caller (CurriculumJobWorker) will write levelBlueprintJson into resultReference
        // AFTER this method returns.  We check for completion by reading jobs that are
        // *already* SUCCEEDED plus the one being marked right now (passed in as the current job).
        // We approximate: count SUCCEEDED jobs + this one (not yet marked).
        List<CfCurriculumPipelineJob> alreadySucceeded = jobRepository
                .findByCurriculumIdAndJobTypeAndStatus(curriculumId, "CURRICULUM_BLUEPRINT", "SUCCEEDED");

        int doneAfterThis = alreadySucceeded.size() + 1; // include current job being completed
        log.info("CURRICULUM_BLUEPRINT: {}/{} level blueprints complete for curriculum={}",
                doneAfterThis, EXPECTED_LEVELS, curriculumId);

        if (doneAfterThis >= EXPECTED_LEVELS) {
            assembleAndPersist(curriculumId, languageDisplayName, languageCode, alreadySucceeded, levelBlueprint);
        }

        return levelBlueprintJson;
    }

    private void assembleAndPersist(UUID curriculumId, String languageDisplayName, String languageCode,
                                     List<CfCurriculumPipelineJob> previousJobs, LevelBlueprint lastLevel) {
        log.info("CURRICULUM_BLUEPRINT: all {} levels done — assembling for curriculum={}",
                EXPECTED_LEVELS, curriculumId);

        // Deserialise all previously SUCCEEDED level blueprints
        List<LevelBlueprint> allLevels = new java.util.ArrayList<>();
        for (CfCurriculumPipelineJob prev : previousJobs) {
            String ref = prev.getResultReference();
            if (ref == null || ref.isBlank()) continue;
            try {
                allLevels.add(MAPPER.readValue(ref, LevelBlueprint.class));
            } catch (Exception e) {
                log.warn("CURRICULUM_BLUEPRINT: failed to deserialise LevelBlueprint for jobId={}: {}",
                        prev.getId(), e.getMessage());
            }
        }
        allLevels.add(lastLevel);

        // Sort by canonical CEFR order
        List<String> cefrOrder = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        allLevels.sort(Comparator.comparingInt(lb -> {
            int idx = cefrOrder.indexOf(lb.cefrLevel());
            return idx < 0 ? 99 : idx;
        }));

        CurriculumBlueprint fullBlueprint = new CurriculumBlueprint(
                languageDisplayName + " Complete Curriculum",
                languageCode,
                allLevels
        );

        log.info("CURRICULUM_BLUEPRINT: assembled {} levels, {} total lessons",
                fullBlueprint.levels().size(), fullBlueprint.totalLessons());

        curriculumService.persistBlueprint(curriculumId, fullBlueprint,
                "v1 blueprint — assembled from " + EXPECTED_LEVELS + " per-level LLM calls");

        log.info("CURRICULUM_BLUEPRINT: blueprint persisted for curriculum={}", curriculumId);

        // Advance: BLUEPRINT_PENDING → BLUEPRINT_GENERATED → BLUEPRINT_VALIDATED → GENERATION_IN_PROGRESS
        orchestrator.applyTransition(curriculumId, "BLUEPRINT_GENERATED",
                "blueprint_generation_complete", "system", null, null);
        orchestrator.applyTransition(curriculumId, "BLUEPRINT_VALIDATED",
                "blueprint_auto_validated", "system", null, null);
        orchestrator.applyTransition(curriculumId, "GENERATION_IN_PROGRESS",
                "generation_started", "system", null, null);

        log.info("CURRICULUM_BLUEPRINT: curriculum={} → GENERATION_IN_PROGRESS", curriculumId);
    }
}
