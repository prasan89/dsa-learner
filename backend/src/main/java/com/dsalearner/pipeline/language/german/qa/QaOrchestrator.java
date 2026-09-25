package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.domain.ContentStatus;
import com.dsalearner.pipeline.model.entity.CfAgentRun;
import com.dsalearner.pipeline.model.entity.CfLesson;
import com.dsalearner.pipeline.model.entity.CfLessonVersion;
import com.dsalearner.pipeline.model.entity.CfPipelineJob;
import com.dsalearner.pipeline.repository.CfAgentRunRepository;
import com.dsalearner.pipeline.repository.CfLessonRepository;
import com.dsalearner.pipeline.repository.CfLessonVersionRepository;
import com.dsalearner.pipeline.service.PipelineJobService;
import com.dsalearner.pipeline.statemachine.WorkflowOrchestrator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Executes a QA_CONTENT pipeline job end-to-end.
 *
 * Flow:
 *   QA_PENDING
 *     → run 4 QA agents in sequence via AgentRunner (idempotency + cost tracking automatic)
 *     → QaAggregator produces PASS / PASS_WITH_WARNINGS / FAIL
 *     → PASS / PASS_WITH_WARNINGS  → QA_PASSED
 *     → FAIL
 *         → revision limit NOT reached  → REVISION
 *         → revision limit reached       → HUMAN_REVIEW_REQUIRED
 *
 * On FAIL: revision_feedback is written to cf_lesson_versions so the next
 * generation job can receive targeted QA issues.
 *
 * QA run IDs are appended to cf_lesson_versions.qa_run_ids for full lineage.
 *
 * This class has no @Transactional — it deliberately holds no DB connection
 * across the external LLM calls.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QaOrchestrator {

    private static final int MAX_QA_REVISIONS = 3;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AgentRunner agentRunner;
    private final GermanLanguageQaAgent languageQaAgent;
    private final CefrQaAgent cefrQaAgent;
    private final ExerciseQaAgent exerciseQaAgent;
    private final PedagogyQaAgent pedagogyQaAgent;
    private final QaAggregator qaAggregator;
    private final PromptRegistry promptRegistry;
    private final ModelRouter modelRouter;
    private final WorkflowOrchestrator workflowOrchestrator;
    private final CfLessonRepository lessonRepository;
    private final CfLessonVersionRepository lessonVersionRepository;
    private final CfAgentRunRepository agentRunRepository;
    private final PipelineJobService pipelineJobService;

    /**
     * Executes QA for the given pipeline job.
     * Returns a comma-separated string of the 4 QA agent run IDs as the job's resultReference.
     */
    public String execute(CfPipelineJob job) {
        UUID lessonId = job.getLessonId();
        int  version  = job.getLessonVersion();

        CfLesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException("Lesson not found: " + lessonId));

        CfLessonVersion lv = lessonVersionRepository.findByLessonIdAndVersion(lessonId, version)
                .orElseThrow(() -> new com.dsalearner.exception.NotFoundException(
                        "LessonVersion not found: lessonId=%s version=%d".formatted(lessonId, version)));

        // Build the lesson JSON that all 4 QA agents will evaluate
        String lessonJson = buildLessonJson(lv, lessonId);
        QaInput qaInput = new QaInput(lessonJson);

        log.info("QaOrchestrator: starting QA for lessonId={} version={}", lessonId, version);

        // ── Run all 4 QA agents ──────────────────────────────────────────────
        List<AgentOutput<QaResult>> outputs = new ArrayList<>();
        List<UUID> runIds = new ArrayList<>();

        for (Agent<QaInput, QaResult> agent : qaAgents()) {
            PromptRegistry.ResolvedPrompt prompt = promptRegistry.resolve(promptKeyFor(agent.agentType()));
            ModelConfig modelConfig = modelRouter.resolve(agent.agentType(), lesson.getDomainCode(), lesson.getLanguageCode());

            AgentOutput<QaResult> output = agentRunner.run(
                    agent,
                    lessonId, version,
                    lesson.getDomainCode(), lesson.getLanguageCode(),
                    qaInput,
                    prompt.id(), prompt.version(), modelConfig.configKey()
            );
            outputs.add(output);
            runIds.add(output.agentRunId());

            log.info("QaOrchestrator: agent={} issues={} lessonId={}",
                    agent.agentType(),
                    output.issues() != null ? output.issues().size() : 0,
                    lessonId);
        }

        // ── Persist qa_decision on each run ──────────────────────────────────
        QaAggregator.Decision decision = qaAggregator.aggregate(outputs);
        String decisionStr = decision.name();

        for (UUID runId : runIds) {
            agentRunRepository.findById(runId).ifPresent(run -> {
                run.setQaDecision(decisionStr);
                agentRunRepository.save(run);
            });
        }

        // ── Update qa_run_ids on the lesson version ───────────────────────────
        persistQaRunIds(lv, runIds, decision, outputs);

        // ── Apply state transition ────────────────────────────────────────────
        if (!qaAggregator.isFail(decision)) {
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.QA_PASSED, "QA_PASSED",
                    "orchestrator:qa", null,
                    Map.of("decision", decisionStr, "agentRunIds", runIds.stream().map(UUID::toString).toList()));

            log.info("QaOrchestrator: QA_PASSED lessonId={} decision={}", lessonId, decisionStr);
        } else {
            // First record the QA_FAILED transition
            workflowOrchestrator.applyContentTransition(
                    lessonId, ContentStatus.QA_FAILED, "QA_FAILED",
                    "orchestrator:qa", null,
                    Map.of("decision", decisionStr, "errorCount", countErrors(outputs)));

            // Write revision_feedback so the next generation agent has targeted context
            persistRevisionFeedback(lv, outputs);

            // Decide: revision or escalate?
            boolean escalated = workflowOrchestrator.incrementRevisionCount(lessonId, "orchestrator:qa");

            if (escalated) {
                workflowOrchestrator.applyContentTransition(
                        lessonId, ContentStatus.HUMAN_REVIEW_REQUIRED, "ESCALATE_TO_HUMAN",
                        "orchestrator:qa", null,
                        Map.of("reason", "REVISION_LIMIT_EXCEEDED"));
                log.warn("QaOrchestrator: HUMAN_REVIEW_REQUIRED lessonId={} — revision limit reached", lessonId);
            } else {
                workflowOrchestrator.applyContentTransition(
                        lessonId, ContentStatus.REVISION, "ROUTE_TO_REVISION",
                        "orchestrator:qa", null,
                        Map.of("revisionCount", lesson.getRevisionCount() + 1));
                // Auto-enqueue the revision job — this drives the automated revision loop
                pipelineJobService.submitRevisionGeneration(lessonId, version, Map.of(), "orchestrator:qa");
                log.info("QaOrchestrator: REVISION lessonId={} — REVISION_GENERATION job enqueued", lessonId);
            }
        }

        return String.join(",", runIds.stream().map(UUID::toString).toList());
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private List<Agent<QaInput, QaResult>> qaAgents() {
        return List.of(languageQaAgent, cefrQaAgent, exerciseQaAgent, pedagogyQaAgent);
    }

    private String promptKeyFor(String agentType) {
        return switch (agentType) {
            case AgentType.LINGUISTIC_QA -> "language.german.a1.qa.language";
            case AgentType.CEFR_QA      -> "language.german.a1.qa.cefr";
            case AgentType.EXERCISE_QA  -> "language.german.a1.qa.exercise";
            case AgentType.PEDAGOGY_QA  -> "language.german.a1.qa.pedagogy";
            default -> throw new IllegalArgumentException("No prompt key for QA agent type: " + agentType);
        };
    }

    private String buildLessonJson(CfLessonVersion lv, UUID lessonId) {
        Map<String, Object> lessonMap = new LinkedHashMap<>();
        if (lv.getContent()    != null) lessonMap.putAll(lv.getContent());
        if (lv.getVocabulary() != null) lessonMap.put("vocabulary", lv.getVocabulary());
        if (lv.getGrammar()    != null) lessonMap.put("grammar",    lv.getGrammar());
        if (lv.getExercises()  != null) lessonMap.put("exercises",  lv.getExercises());
        if (lv.getBlueprint()  != null) lessonMap.put("metadata",   lv.getBlueprint());

        try {
            return MAPPER.writeValueAsString(lessonMap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "QaOrchestrator: failed to serialize lesson content for lessonId=" + lessonId, e);
        }
    }

    private void persistQaRunIds(CfLessonVersion lv, List<UUID> runIds,
                                  QaAggregator.Decision decision,
                                  List<AgentOutput<QaResult>> outputs) {
        Map<String, Object> qaRunIds = new LinkedHashMap<>(
                lv.getQaRunIds() != null ? lv.getQaRunIds() : Map.of());

        List<Agent<QaInput, QaResult>> agents = qaAgents();
        for (int i = 0; i < agents.size() && i < runIds.size(); i++) {
            qaRunIds.put(agents.get(i).agentType(), runIds.get(i).toString());
        }
        qaRunIds.put("_decision", decision.name());

        lv.setQaRunIds(qaRunIds);
        lessonVersionRepository.save(lv);
    }

    private void persistRevisionFeedback(CfLessonVersion lv, List<AgentOutput<QaResult>> outputs) {
        List<Map<String, Object>> allIssues = new ArrayList<>();
        for (AgentOutput<QaResult> output : outputs) {
            if (output.issues() == null) continue;
            for (var issue : output.issues()) {
                Map<String, Object> issueMap = new LinkedHashMap<>();
                issueMap.put("agent",     output.output() != null ? "qa" : "unknown");
                issueMap.put("severity",  issue.severity().name());
                issueMap.put("field",     issue.field());
                issueMap.put("message",   issue.message());
                if (issue.suggestion() != null) issueMap.put("suggestion", issue.suggestion());
                allIssues.add(issueMap);
            }
        }

        lv.setRevisionFeedback(Map.of("issues", allIssues));
        lessonVersionRepository.save(lv);
    }

    private long countErrors(List<AgentOutput<QaResult>> outputs) {
        return outputs.stream()
                .filter(o -> o.issues() != null)
                .flatMap(o -> o.issues().stream())
                .filter(i -> i.severity() == com.dsalearner.pipeline.agent.Issue.Severity.ERROR)
                .count();
    }
}
