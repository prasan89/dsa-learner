package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentOutput;
import com.dsalearner.pipeline.agent.Issue;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deterministic QA decision aggregator.
 *
 * Rules (applied in priority order):
 *   1. Any ERROR from any agent  → FAIL
 *   2. Any WARNING, no ERROR     → PASS_WITH_WARNINGS
 *   3. No issues at all          → PASS
 *
 * Evidence validation: LLM-reported ERRORs with no supporting evidence (evidence == null/blank)
 * are downgraded to WARNING before the decision is computed. This prevents false-positive FAILs
 * from LLMs that flag issues without citing the lesson text.
 *
 * This component contains no LLM calls; it only reads the issues lists that
 * AgentRunner has already persisted from each of the four QA agents.
 */
@Component
public class QaAggregator {

    public enum Decision { PASS, PASS_WITH_WARNINGS, FAIL }

    /**
     * Aggregates a list of QA agent outputs into a single decision.
     * ERRORs lacking evidence are demoted to WARNING before the decision.
     */
    public Decision aggregate(List<AgentOutput<QaResult>> qaOutputs) {
        return aggregate(qaOutputs, null);
    }

    /**
     * Aggregates QA agent outputs into a single decision, incorporating the result of
     * deterministic cross-validation. An LLM ERROR on an exercise whose correctAnswer
     * the cross-validator has cleared (grounded in lesson content) is downgraded to WARNING,
     * providing a second layer of false-positive protection beyond the evidence field check.
     */
    public Decision aggregate(List<AgentOutput<QaResult>> qaOutputs,
                               ExerciseAnswerCrossValidator.Result crossValidation) {
        boolean hasErrors   = false;
        boolean hasWarnings = false;

        for (AgentOutput<QaResult> output : qaOutputs) {
            List<Issue> issues = output.issues();
            if (issues == null) continue;
            for (Issue issue : issues) {
                Issue.Severity effective = resolveEffectiveSeverity(issue, crossValidation);
                if (effective == Issue.Severity.ERROR)   { hasErrors = true; }
                if (effective == Issue.Severity.WARNING) { hasWarnings = true; }
            }
        }

        if (hasErrors)   return Decision.FAIL;
        if (hasWarnings) return Decision.PASS_WITH_WARNINGS;
        return Decision.PASS;
    }

    /** Convenience: true when the decision blocks content from advancing to QA_PASSED. */
    public boolean isFail(Decision decision) {
        return decision == Decision.FAIL;
    }

    /**
     * Resolves the effective severity of an issue, applying both evidence-based downgrade
     * and cross-validation downgrade rules.
     *
     * Rule 1 (evidence): ERROR with no evidence → WARNING.
     * Rule 2 (cross-validation): ERROR on a cross-validator-cleared exercise answer → WARNING.
     *
     * @param issue           the issue to evaluate
     * @param crossValidation deterministic cross-validation result (may be null)
     */
    public Issue.Severity resolveEffectiveSeverity(Issue issue,
                                                    ExerciseAnswerCrossValidator.Result crossValidation) {
        if (issue.isUnsupported()) return Issue.Severity.WARNING;
        // Cross-validation: if the issue targets an exercise field and the cross-validator
        // has cleared that answer, downgrade — the lesson itself teaches that form.
        if (crossValidation != null && issue.severity() == Issue.Severity.ERROR
                && issue.field() != null && issue.field().startsWith("exercises[")) {
            int idx = extractExerciseIndex(issue.field());
            String answer = extractAnswerHint(issue.message());
            if (idx >= 0 && answer != null && crossValidation.isCleared(idx, answer)) {
                return Issue.Severity.WARNING;
            }
        }
        return issue.severity();
    }

    /** Package-visible alias used internally. */
    Issue.Severity effectiveSeverity(Issue issue) {
        return resolveEffectiveSeverity(issue, null);
    }

    /** Extracts the numeric index from a field like "exercises[3].correctAnswer". */
    private static int extractExerciseIndex(String field) {
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("exercises\\[(\\d+)\\]").matcher(field);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    /**
     * Tries to extract a quoted answer from an issue message, e.g.:
     * "The correct answer 'Magst du Tee?' contradicts..." → "Magst du Tee?"
     * Returns null if no quoted string is found.
     */
    private static String extractAnswerHint(String message) {
        if (message == null) return null;
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("[''']([^''']+)[''']").matcher(message);
        return m.find() ? m.group(1) : null;
    }
}
