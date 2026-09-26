package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.curriculum.agent.*;
import com.dsalearner.pipeline.curriculum.service.CurriculumLevelService;
import com.dsalearner.pipeline.curriculum.service.CurriculumWorkflowOrchestrator;
import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import com.dsalearner.pipeline.model.entity.CfCurriculumPipelineJob;
import com.dsalearner.pipeline.repository.CfCurriculumLessonPlanRepository;
import com.dsalearner.pipeline.repository.CfCurriculumLevelRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Executes CURRICULUM_LEVEL_QA job: runs deterministic checks + LLM QA on a level,
 * then transitions the level to LEVEL_QA_PASSED or LEVEL_QA_FAILED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumLevelQaOrchestrator {

    private final CurriculumLevelQaAgent levelQaAgent;
    private final AgentRunner agentRunner;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final CurriculumLevelService levelService;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final CfCurriculumLevelRepository levelRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String execute(CfCurriculumPipelineJob job) {
        UUID curriculumId = job.getCurriculumId();
        UUID levelId      = job.getLevelId();
        Map<String, Object> payload = job.getPayload();

        String languageCode        = (String) payload.get("languageCode");
        String languageDisplayName = (String) payload.getOrDefault("languageDisplayName", languageCode);
        String domainCode          = (String) payload.getOrDefault("domainCode", "language");

        CfCurriculumLevel level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found: " + levelId));

        // Build level summary JSON from lesson plans
        List<CfCurriculumLessonPlan> plans =
                lessonPlanRepository.findByLevelIdOrderByPosition(levelId);
        String levelSummaryJson = buildLevelSummary(plans);
        String deterministicSummary = runDeterministicChecks(plans);

        CurriculumQaInput qaInput = new CurriculumQaInput(
                languageCode, languageDisplayName, level.getCefrLevel(),
                levelSummaryJson, "{}", deterministicSummary
        );

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(AgentType.CURRICULUM_LEVEL_QA);
        ModelConfig modelConfig = modelRouter.resolve(AgentType.CURRICULUM_LEVEL_QA, domainCode, languageCode);
        UUID runId = UUID.randomUUID();

        AgentInput<CurriculumQaInput> agentInput = new AgentInput<>(
                runId, curriculumId, 1, UUID.randomUUID().toString(), qaInput,
                new AgentInput.AgentContext(domainCode, languageCode, level.getCefrLevel(),
                        null, 0, 3, prompt.id(), prompt.version(), modelConfig.configKey())
        );

        AgentOutput<CurriculumQaResult> output = levelQaAgent.execute(agentInput);
        CurriculumQaResult result = output.output();
        boolean passed = result != null && result.passed();

        // State machine requires GENERATION_IN_PROGRESS → LEVEL_QA_PENDING → LEVEL_QA_PASSED/FAILED
        orchestrator.applyLevelTransition(levelId, "LEVEL_QA_PENDING",
                "level_qa_started", "system", null, null);
        levelService.applyQaResult(levelId, passed, runId, "system");
        log.info("Level QA for {} {} — decision={}", languageCode, level.getCefrLevel(),
                result != null ? result.decision() : "null");

        return runId.toString();
    }

    private String buildLevelSummary(List<CfCurriculumLessonPlan> plans) {
        try {
            return MAPPER.writeValueAsString(plans.stream().map(p -> Map.of(
                    "position", p.getPosition(),
                    "stableRef", p.getStableRef(),
                    "title", p.getTitle(),
                    "topic", p.getTopic(),
                    "status", p.getPlanStatus()
            )).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String runDeterministicChecks(List<CfCurriculumLessonPlan> plans) {
        StringBuilder sb = new StringBuilder();
        int total = plans.size();
        long approved = plans.stream().filter(p ->
                "QA_PASSED".equals(p.getPlanStatus()) || "APPROVED".equals(p.getPlanStatus())).count();
        long failed = plans.stream().filter(p ->
                "QA_FAILED".equals(p.getPlanStatus()) || "FAILED".equals(p.getPlanStatus())).count();

        sb.append("Total plans: ").append(total).append("\n");
        sb.append("Approved/QA-passed: ").append(approved).append("\n");
        sb.append("Failed: ").append(failed).append("\n");

        if (failed > 0) {
            sb.append("WARNING: ").append(failed).append(" plan(s) in failed state\n");
        }
        long missingStableRef = plans.stream().filter(p -> p.getStableRef() == null).count();
        if (missingStableRef > 0) {
            sb.append("ERROR: ").append(missingStableRef).append(" plan(s) missing stableRef\n");
        }
        return sb.toString();
    }
}
