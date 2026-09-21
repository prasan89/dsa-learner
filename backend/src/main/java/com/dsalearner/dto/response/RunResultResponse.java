package com.dsalearner.dto.response;

import java.util.List;

public record RunResultResponse(
        String status,
        String errorMessage,
        Integer runtimeMs,
        List<TestResultResponse> testResults
) {
    public record TestResultResponse(
            String testCaseId,
            boolean passed,
            String input,
            String expectedOutput,
            String actualOutput,
            Integer executionTimeMs
    ) {}
}
