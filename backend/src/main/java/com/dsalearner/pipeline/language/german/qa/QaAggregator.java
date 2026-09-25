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
 * This component contains no LLM calls; it only reads the issues lists that
 * AgentRunner has already persisted from each of the four QA agents.
 */
@Component
public class QaAggregator {

    public enum Decision { PASS, PASS_WITH_WARNINGS, FAIL }

    /**
     * Aggregates a list of QA agent outputs into a single decision.
     */
    public Decision aggregate(List<AgentOutput<QaResult>> qaOutputs) {
        boolean hasErrors   = false;
        boolean hasWarnings = false;

        for (AgentOutput<QaResult> output : qaOutputs) {
            List<Issue> issues = output.issues();
            if (issues == null) continue;
            for (Issue issue : issues) {
                if (issue.severity() == Issue.Severity.ERROR)   { hasErrors = true; }
                if (issue.severity() == Issue.Severity.WARNING) { hasWarnings = true; }
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
}
