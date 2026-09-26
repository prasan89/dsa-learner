package com.dsalearner.pipeline.curriculum.agent;

import java.util.List;

/**
 * Input to the CurriculumBlueprintAgent.
 * Language-agnostic — resolved from LanguageProfile.
 */
public record BlueprintInput(
        String languageCode,
        String languageDisplayName,
        String script,
        String domainCode,
        List<String> cefrLevels,        // ["A1","A2","B1","B2","C1","C2"]
        String curriculumGoals,          // free-text goals from the user/system
        String proficiencyFramework      // "CEFR" default; extensible to JLPT, HSK etc.
) {
    public static BlueprintInput forCefr(String languageCode, String languageDisplayName,
                                          String script, String domainCode, String goals) {
        return new BlueprintInput(
                languageCode, languageDisplayName, script, domainCode,
                List.of("A1", "A2", "B1", "B2", "C1", "C2"),
                goals,
                "CEFR"
        );
    }
}
