package com.dsalearner.dto.response;

public record AiMentorResponse(
        String message,
        String type  // "guidance", "hint", "question", "encouragement"
) {}
