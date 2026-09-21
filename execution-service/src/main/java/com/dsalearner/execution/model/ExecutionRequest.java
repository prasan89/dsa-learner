package com.dsalearner.execution.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ExecutionRequest {
    @NotBlank
    private String code;
    private String language = "JAVA";
    @NotEmpty
    private List<TestCaseInput> testCases;
    private int timeLimitMs = 5000;
    private int memoryLimitMb = 128;

    @Data
    public static class TestCaseInput {
        private String id;
        @NotBlank
        private String input;
        @NotBlank
        private String expectedOutput;
    }
}
