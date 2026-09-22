package com.dsalearner.dto.response;

public record DashboardResponse(
    int streak,
    DsaStats dsa,
    SystemDesignStats systemDesign,
    String plan,
    NextAction nextAction
) {
    public record DsaStats(long total, long solved, double masteryAvg) {}
    public record SystemDesignStats(long total, long mastered, double masteryAvg) {}
    public record NextAction(String slug, String title, String patternName, String difficulty) {}
}
