package com.dsalearner.dto.response;

import com.dsalearner.model.enums.SubmissionStatus;

import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        UUID problemId,
        String problemSlug,
        String problemTitle,
        SubmissionStatus status,
        String language,
        String code,
        Integer runtimeMs,
        Integer memoryKb,
        String errorMessage,
        Instant submittedAt
) {}
