package com.dsalearner.execution.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class ExecutionResult {
    private String status;
    private Integer runtimeMs;
    private Integer memoryKb;
    private String errorMessage;
    private List<TestCaseResult> testResults;

    @Data @Builder
    public static class TestCaseResult {
        private String testCaseId;
        private boolean passed;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private Integer executionTimeMs;
    }
}
