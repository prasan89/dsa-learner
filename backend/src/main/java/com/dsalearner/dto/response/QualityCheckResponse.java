package com.dsalearner.dto.response;

import java.util.List;
import java.util.Map;

public record QualityCheckResponse(
        String slug,
        String contentStatus,
        int passCount,
        int totalChecks,
        boolean allMandatoryPass,
        List<CheckItem> checks
) {
    public record CheckItem(String key, String label, boolean pass, boolean mandatory) {}

    public static QualityCheckResponse from(String slug, String contentStatus,
                                             Map<String, Boolean> flags) {
        List<CheckItem> checks = CHECKLIST.stream()
                .map(c -> new CheckItem(c.key(), c.label(), flags != null && Boolean.TRUE.equals(flags.get(c.key())), c.mandatory()))
                .toList();

        long passCount = checks.stream().filter(CheckItem::pass).count();
        boolean allMandatoryPass = checks.stream()
                .filter(CheckItem::mandatory)
                .allMatch(CheckItem::pass);

        return new QualityCheckResponse(slug, contentStatus,
                (int) passCount, checks.size(), allMandatoryPass, checks);
    }

    private record CheckDef(String key, String label, boolean mandatory) {}

    private static final List<CheckDef> CHECKLIST = List.of(
            new CheckDef("has_recognition_note",          "Pattern recognition note",       true),
            new CheckDef("has_pattern_recognition_clues", "Recognition clues (keywords)",   true),
            new CheckDef("has_when_to_use",               "When to use",                    true),
            new CheckDef("has_when_not_to_use",           "When NOT to use",                true),
            new CheckDef("has_intuition",                 "Level 1 — Intuition",            true),
            new CheckDef("has_guided_reasoning",          "Level 2 — Guided reasoning",     true),
            new CheckDef("has_solution",                  "Level 3 — Solution (Java)",      true),
            new CheckDef("has_brute_force",               "Brute force approach",           true),
            new CheckDef("has_brute_complexity",          "Brute force complexity",         true),
            new CheckDef("has_optimal_approach",          "Optimal approach",               true),
            new CheckDef("has_optimal_complexity",        "Optimal complexity",             true),
            new CheckDef("has_pseudocode",                "Pseudocode",                     true),
            new CheckDef("has_why_this_works",            "Why this works",                 true),
            new CheckDef("has_invariant",                 "Invariant",                      true),
            new CheckDef("has_common_mistakes",           "Common mistakes",                true),
            new CheckDef("has_senior_variations",         "Senior variations",              false)
    );
}
