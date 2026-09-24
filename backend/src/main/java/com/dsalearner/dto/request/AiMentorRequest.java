package com.dsalearner.dto.request;

import java.util.List;

public record AiMentorRequest(
        String conceptId,
        String conceptTitle,
        String problemSlug,
        String currentCode,
        String executionResult,
        String compilerError,
        int attemptCount,
        int hintsUsed,
        String masteryLevel,
        String userMessage,
        List<MessageEntry> previousMessages
) {
    public record MessageEntry(String role, String content) {}
}
