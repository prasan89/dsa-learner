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
    void anyError_returnsFail() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(Issue.warning("W1", "f", "w"))),
                outputWithIssues(List.of(Issue.error("E1", "field", "critical error")))
        ));
        assertEquals(QaAggregator.Decision.FAIL, result);
        assertTrue(aggregator.isFail(result));
    }

    @Test
    void multipleErrors_returnsFail() {
        var result = aggregator.aggregate(List.of(
                outputWithIssues(List.of(
                        Issue.error("E1", "f1", "e1"),
                        Issue.error("E2", "f2", "e2")
                ))
        ));
        assertEquals(QaAggregator.Decision.FAIL, result);
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
        // INFO does not count as warning or error
        assertEquals(QaAggregator.Decision.PASS, result);
    }
}
