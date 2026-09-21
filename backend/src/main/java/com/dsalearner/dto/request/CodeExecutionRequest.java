package com.dsalearner.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CodeExecutionRequest(
        @NotBlank String code,
        String language
) {
    public CodeExecutionRequest {
        if (language == null) language = "JAVA";
    }
}
