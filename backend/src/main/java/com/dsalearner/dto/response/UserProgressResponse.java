package com.dsalearner.dto.response;

public record UserProgressResponse(
        long totalSolved,
        long easySolved,
        long mediumSolved,
        long hardSolved
) {}
