package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.AgentOutput;
import com.dsalearner.pipeline.agent.Issue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the deterministic QaAggregator. */
class QaAggregatorTest {

    private final QaAggregator aggregator = new QaAggregator();

    private AgentOutput<QaResult> outputWithIssues(List<Issue> issues) {
        QaResult result = new QaResult("assessment", issues, List.of());
        return new AgentOutput<>(UUID.randomUUID(), AgentOutput.Status.SUCCEEDED,
                result, 0.9, issues, List.of(), false, null);
    }

    @Test
    void noIssues_returnsPass() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of()),
                outputWithIssues(List.of())
        ));
        assertEquals(QaAggregator.Decision.PASS, result);
        assertFalse(aggregator.isFail(result));
    }

    @Test
    void warningsOnly_returnsPassWithWarnings() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(Issue.warning("W1", "field", "msg"))),
                outputWithIssues(List.of())
        ));
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, result);
        assertFalse(aggregator.isFail(result));
    }

    @Test
    void supportedError_returnsFail() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(Issue.warning("W1", "f", "w"))),
                outputWithIssues(List.of(Issue.errorWithEvidence("E1", "exercises[0].correctAnswer",
                        "critical error", "grammar.examples[0].german = 'Ich bin'")))
        ));
        assertEquals(QaAggregator.Decision.FAIL, result);
        assertTrue(aggregator.isFail(result));
    }

    @Test
    void multipleSupportedErrors_returnsFail() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(
                        Issue.errorWithEvidence("E1", "f1", "e1", "lesson says X"),
                        Issue.errorWithEvidence("E2", "f2", "e2", "lesson says Y")
                ))
        ));
        assertEquals(QaAggregator.Decision.FAIL, result);
    }

    @Test
    void unsupportedError_noEvidence_downgradedToWarning() {
        // Issue.error() produces no evidence → isUnsupported() → downgraded to WARNING
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(Issue.error("E1", "exercises[0]", "fake error")))
        ));
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, result,
                "An ERROR with no evidence must be downgraded to WARNING, yielding PASS_WITH_WARNINGS");
        assertFalse(aggregator.isFail(result));
    }

    @Test
    void unsupportedError_blankEvidence_downgradedToWarning() {
        var blankEvidenceError = new Issue("E1", Issue.Severity.ERROR, "exercises[2]",
                "flagged without evidence", null, false, "   ");
        var result = aggregator.aggregate(List.of(outputWithIssues(List.of(blankEvidenceError))));
        assertEquals(QaAggregator.Decision.PASS_WITH_WARNINGS, result,
                "An ERROR with blank evidence must be downgraded to WARNING");
    }

    @Test
    void emptyList_returnsPass() {
        assertEquals(QaAggregator.Decision.PASS, aggregator.aggregate(List.of()));
    }

    @Test
    void nullIssuesList_handledGracefully() {
        AgentOutput<QaResult> output = new AgentOutput<>(
                UUID.randomUUID(), AgentOutput.Status.SUCCEEDED,
                new QaResult("ok", List.of(), List.of()),
                0.9, null, List.of(), false, null);
        assertEquals(QaAggregator.Decision.PASS, aggregator.aggregate(List.of(output)));
    }

    @Test
    void infoOnly_returnsPass() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(Issue.info("I1", "f", "info msg")))
        ));
        assertEquals(QaAggregator.Decision.PASS, result);
    }

    // ── resolveEffectiveSeverity unit tests ──────────────────────────────────

    @Test
    void effectiveSeverity_supportedError_staysError() {
        Issue e = Issue.errorWithEvidence("E", "exercises[0].correctAnswer", "msg", "lesson text X");
        assertEquals(Issue.Severity.ERROR, aggregator.resolveEffectiveSeverity(e, null));
    }

    @Test
    void effectiveSeverity_unsupportedError_becomesWarning() {
        Issue e = Issue.error("E", "exercises[0]", "flagged without evidence");
        assertEquals(Issue.Severity.WARNING, aggregator.resolveEffectiveSeverity(e, null));
    }

    @Test
    void effectiveSeverity_warning_unchangedEvenWithoutEvidence() {
        Issue w = Issue.warning("W", "field", "a warning");
        assertEquals(Issue.Severity.WARNING, aggregator.resolveEffectiveSeverity(w, null));
    }

    @Test
    void effectiveSeverity_info_unchanged() {
        Issue i = Issue.info("I", "field", "info");
        assertEquals(Issue.Severity.INFO, aggregator.resolveEffectiveSeverity(i, null));
    }
}
