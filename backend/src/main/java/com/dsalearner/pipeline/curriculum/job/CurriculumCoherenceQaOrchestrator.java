package com.dsalearner.pipeline.curriculum.job;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.curriculum.agent.*;
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
 * Executes CURRICULUM_COHERENCE_QA job: cross-level coherence check.
 * Transitions curriculum to CURRICULUM_QA_PASSED or CURRICULUM_QA_FAILED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CurriculumCoherenceQaOrchestrator {

    private final CurriculumCoherenceQaAgent coherenceQaAgent;
    private final AgentRunner agentRunner;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final CurriculumWorkflowOrchestrator orchestrator;
    private final CfCurriculumLevelRepository levelRepository;
    private final CfCurriculumLessonPlanRepository lessonPlanRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String execute(CfCurriculumPipelineJob job) {
        UUID curriculumId = job.getCurriculumId();
        Map<String, Object> payload = job.getPayload();

        String languageCode        = (String) payload.get("languageCode");
        String languageDisplayName = (String) payload.getOrDefault("languageDisplayName", languageCode);
        String domainCode          = (String) payload.getOrDefault("domainCode", "language");

        List<CfCurriculumLevel> levels = levelRepository.findByCurriculumIdOrderByOrdinal(curriculumId);
        String curriculumSummaryJson = buildCurriculumSummary(curriculumId, levels);
        String deterministicSummary = runDeterministicChecks(levels);

        CurriculumQaInput qaInput = new CurriculumQaInput(
                languageCode, languageDisplayName, null,
                null, curriculumSummaryJson, deterministicSummary
        );

        PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(AgentType.CURRICULUM_COHERENCE_QA);
        ModelConfig modelConfig = modelRouter.resolve(AgentType.CURRICULUM_COHERENCE_QA, domainCode, languageCode);
        UUID runId = UUID.randomUUID();

        AgentInput<CurriculumQaInput> agentInput = new AgentInput<>(
                runId, curriculumId, 1, UUID.randomUUID().toString(), qaInput,
                new AgentInput.AgentContext(domainCode, languageCode, null,
                        null, 0, 3, prompt.id(), prompt.version(), modelConfig.configKey())
        );

        AgentOutput<CurriculumQaResult> output = coherenceQaAgent.execute(agentInput);
        CurriculumQaResult result = output.output();
        boolean passed = result != null && result.passed();

        String toStatus = passed ? "CURRICULUM_QA_PASSED" : "CURRICULUM_QA_FAILED";
        orchestrator.applyTransition(curriculumId, toStatus,
                "coherence_qa_result", "system", runId, null);

        log.info("Curriculum coherence QA for {} — decision={}", languageCode,
                result != null ? result.decision() : "null");
        return runId.toString();
    }

    private String buildCurriculumSummary(UUID curriculumId, List<CfCurriculumLevel> levels) {
        try {
            return MAPPER.writeValueAsString(levels.stream().map(l -> {
                List<CfCurriculumLessonPlan> plans =
                        lessonPlanRepository.findByLevelIdOrderByPosition(l.getId());
                return Map.of(
                        "cefrLevel", l.getCefrLevel(),
                        "status", l.getLevelStatus(),
                        "lessonCount", plans.size(),
                        "approvedCount", plans.stream().filter(p ->
                                "QA_PASSED".equals(p.getPlanStatus()) || "APPROVED".equals(p.getPlanStatus())).count()
                );
            }).collect(Collectors.toList()));
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private String runDeterministicChecks(List<CfCurriculumLevel> levels) {
        StringBuilder sb = new StringBuilder();
        long passed = levels.stream().filter(l ->
                "LEVEL_QA_PASSED".equals(l.getLevelStatus()) || "APPROVED".equals(l.getLevelStatus())).count();
        sb.append("Levels: ").append(levels.size()).append(", Level-QA-passed: ").append(passed).append("\n");
        if (passed < levels.size()) {
            sb.append("WARNING: Not all levels are QA-passed\n");
        }
        return sb.toString();
    }
}
