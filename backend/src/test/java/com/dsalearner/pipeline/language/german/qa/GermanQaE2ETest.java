package com.dsalearner.pipeline.language.german.qa;

import com.dsalearner.pipeline.agent.*;
import com.dsalearner.pipeline.provider.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real Anthropic E2E QA run using claude-haiku-4-5-20251001.
 *
 * Run with: ANTHROPIC_API_KEY=sk-ant-... mvn test -Dgroups=e2e
 *
 * This test is skipped unless ANTHROPIC_API_KEY is set.
 * It calls the real Anthropic API and will consume tokens.
 *
 * Results (tokens, cost, latency, decision) are printed to stdout.
 */
@Tag("e2e")
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class GermanQaE2ETest {

    private static final String MODEL_ID    = "claude-haiku-4-5-20251001";
    private static final int    MAX_TOKENS  = 2048;
    private static final double TEMPERATURE = 0.10;
    private static final int    TIMEOUT_MS  = 60_000;

    // Cost rates for claude-haiku-4-5 (per 1K tokens)
    private static final BigDecimal COST_INPUT_PER_1K  = new BigDecimal("0.00025");
    private static final BigDecimal COST_OUTPUT_PER_1K = new BigDecimal("0.00125");

    @Test
    void realAnthropicQaRun_onMockA1Lesson() {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");
        String baseUrl = System.getenv().getOrDefault("ANTHROPIC_BASE_URL", "https://api.anthropic.com");

        AnthropicLlmProvider provider = new AnthropicLlmProvider(
                apiKey, baseUrl, WebClient.builder());
        LlmProviderRegistry registry = new LlmProviderRegistry(List.of(provider));

        ModelConfig modelConfig = new ModelConfig(
                "haiku_qa_v1", "anthropic", MODEL_ID,
                TEMPERATURE, MAX_TOKENS, TIMEOUT_MS,
                COST_INPUT_PER_1K, COST_OUTPUT_PER_1K);

        String lessonJson = MockLlmProvider.MOCK_GERMAN_A1_LESSON;
        QaInput qaInput = new QaInput(lessonJson);

        // Agent type → prompt key map
        record AgentSpec(String type, String promptKey) {}
        List<AgentSpec> specs = List.of(
                new AgentSpec(AgentType.LINGUISTIC_QA, "language.german.a1.qa.language"),
                new AgentSpec(AgentType.CEFR_QA,       "language.german.a1.qa.cefr"),
                new AgentSpec(AgentType.EXERCISE_QA,   "language.german.a1.qa.exercise"),
                new AgentSpec(AgentType.PEDAGOGY_QA,   "language.german.a1.qa.pedagogy")
        );

        // Use real prompts from V36 (embedded here to avoid DB dependency in this test)
        var prompts = realPrompts();

        int totalInput   = 0;
        int totalOutput  = 0;
        long totalMs     = 0;
        List<AgentOutput<QaResult>> outputs = new ArrayList<>();
        UUID lessonId = UUID.randomUUID();

        System.out.println("\n══════════════════════════════════════════════════");
        System.out.println("Phase 1B — Real Anthropic QA E2E Run");
        System.out.println("Model: " + MODEL_ID);
        System.out.println("Lesson: Greetings and Introductions (A1 German)");
        System.out.println("══════════════════════════════════════════════════");

        for (AgentSpec spec : specs) {
            PromptRegistry.ResolvedPrompt prompt = prompts.get(spec.type());
            assertNotNull(prompt, "Missing prompt for agent type: " + spec.type());

            String userPrompt = prompt.promptText().replace("{{lessonJson}}", lessonJson);

            long start = System.currentTimeMillis();
            LlmResponse resp = provider.generate(new LlmRequest(
                    MODEL_ID, prompt.systemPrompt(), userPrompt, MAX_TOKENS, TEMPERATURE, TIMEOUT_MS));
            long latencyMs = System.currentTimeMillis() - start;

            totalInput  += resp.inputTokens();
            totalOutput += resp.outputTokens();
            totalMs     += latencyMs;

            // Parse the response directly (bypassing the agent's execute method since we have no bean context)
            QaResult result = parseQaResponse(resp.text(), spec.type(), lessonId);
            outputs.add(new AgentOutput<>(UUID.randomUUID(), AgentOutput.Status.SUCCEEDED,
                    result, result.hasErrors() ? 0.6 : 0.95,
                    result.issues() != null ? result.issues() : List.of(),
                    result.recommendations() != null ? result.recommendations() : List.of(),
                    false, null));

            double cost = modelConfig.estimateCost(resp.inputTokens(), resp.outputTokens());
            System.out.printf("%n[%s]%n", spec.type().toUpperCase());
            System.out.printf("  tokens in=%-5d out=%-5d  latency=%dms  cost=$%.6f%n",
                    resp.inputTokens(), resp.outputTokens(), latencyMs, cost);
            System.out.printf("  assessment: %s%n", result.overallAssessment());
            System.out.printf("  issues: %d  recommendations: %d%n",
                    result.issues().size(), result.recommendations().size());
            if (!result.issues().isEmpty()) {
                result.issues().forEach(i ->
                    System.out.printf("    [%s] %s — %s%n", i.severity(), i.field(), i.message()));
            }
        }

        QaAggregator aggregator = new QaAggregator();
        QaAggregator.Decision decision = aggregator.aggregate(outputs);

        double totalCost = modelConfig.estimateCost(totalInput, totalOutput);

        System.out.println("\n══════════════════════════════════════════════════");
        System.out.printf("TOTALS: tokens in=%d out=%d  latency=%dms  cost=$%.6f%n",
                totalInput, totalOutput, totalMs, totalCost);
        System.out.printf("DECISION: %s%n", decision);
        System.out.println("══════════════════════════════════════════════════\n");

        // Assertions: mock lesson should pass QA or pass with warnings (not fail)
        assertNotEquals(QaAggregator.Decision.FAIL, decision,
                "Mock A1 lesson should not have ERROR-level QA issues from Anthropic; decision was: " + decision);
        assertTrue(totalInput > 0, "Expected non-zero input tokens");
        assertTrue(totalOutput > 0, "Expected non-zero output tokens");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private java.util.Map<String, PromptRegistry.ResolvedPrompt> realPrompts() {
        // Embedded prompts matching V36 migration exactly (avoids DB dependency in this test)
        var map = new java.util.LinkedHashMap<String, PromptRegistry.ResolvedPrompt>();

        map.put(AgentType.LINGUISTIC_QA, new PromptRegistry.ResolvedPrompt(
                UUID.randomUUID(), "language.german.a1.qa.language", 1,
                "You are an expert German language teacher and linguist evaluating A1 lesson content for language correctness.\n\nEvaluate ONLY German language quality. Do not evaluate pedagogy, CEFR level, or exercise design.\n\nBe constructive. Minor stylistic variations in natural German are acceptable.\nOnly flag genuine errors in grammar, spelling, articles, verb conjugation, word order, or vocabulary.\n\nOutput ONLY valid JSON. No markdown, no explanation outside the JSON.",
                "Evaluate this German A1 lesson for German language quality.\n\nLesson content:\n{{lessonJson}}\n\nEvaluate:\n1. German grammar correctness (articles, verb conjugation, case usage, word order)\n2. Spelling and orthography (including ä, ö, ü, ß)\n3. Sentence correctness and naturalness\n4. Article usage (der/die/das)\n5. Verb conjugation accuracy\n6. Vocabulary correctness (correct German words, not false cognates)\n7. Natural German phrasing (not word-for-word translated from English)\n\nOutput a JSON object with this EXACT structure:\n{\n  \"overallAssessment\": \"string (1-2 sentence summary)\",\n  \"issues\": [\n    {\n      \"severity\": \"ERROR|WARNING|INFO\",\n      \"field\": \"string\",\n      \"message\": \"string\",\n      \"originalText\": \"string\",\n      \"suggestion\": \"string\"\n    }\n  ],\n  \"recommendations\": [\"string\"]\n}\n\nIf there are no issues, return an empty issues array."
        ));

        map.put(AgentType.CEFR_QA, new PromptRegistry.ResolvedPrompt(
                UUID.randomUUID(), "language.german.a1.qa.cefr", 1,
                "You are a CEFR language learning expert evaluating whether lesson content is appropriate for A1 level learners.\n\nA1 learners are absolute beginners. They know virtually no German.\nA1 content should use: very common vocabulary, present tense, simple sentence structures, familiar everyday topics.\n\nDo NOT over-restrict. A1 does not mean every word must be from a fixed 500-word list.\nNatural beginner content using common words is acceptable even if not in the strictest A1 vocabulary lists.\nFlag GENUINE level mismatches — complex grammar structures, advanced vocabulary, B1+ sentence complexity.\n\nOutput ONLY valid JSON.",
                "Evaluate this German lesson for CEFR A1 appropriateness.\n\nLesson content:\n{{lessonJson}}\n\nEvaluate:\n1. Vocabulary difficulty — are words appropriate for absolute beginners?\n2. Grammar difficulty — are grammatical structures A1-appropriate (present tense, basic sentence patterns)?\n3. Sentence complexity — are sentences short and clear enough for A1?\n4. Learning objectives — are they achievable at A1?\n5. Exercise difficulty — are exercises appropriate for beginners?\n6. Overall progression — does the lesson feel like genuine A1 content?\n\nOutput a JSON object with this EXACT structure:\n{\n  \"overallAssessment\": \"string\",\n  \"cefrLevelVerified\": \"A1\",\n  \"issues\": [\n    {\n      \"severity\": \"ERROR|WARNING|INFO\",\n      \"field\": \"string\",\n      \"message\": \"string\",\n      \"suggestion\": \"string\"\n    }\n  ],\n  \"recommendations\": [\"string\"]\n}"
        ));

        map.put(AgentType.EXERCISE_QA, new PromptRegistry.ResolvedPrompt(
                UUID.randomUUID(), "language.german.a1.qa.exercise", 1,
                "You are a language learning exercise designer evaluating the quality and correctness of exercises in a German A1 lesson.\n\nEvaluate exercise correctness rigorously. Wrong answers or ambiguous questions undermine learner trust.\n\nOutput ONLY valid JSON.",
                "Evaluate the exercises in this German A1 lesson.\n\nFull lesson content:\n{{lessonJson}}\n\nEvaluate each exercise for:\n1. Does the exercise match the lesson topic and vocabulary taught?\n2. Is the correct answer actually correct?\n3. For MULTIPLE_CHOICE: are all incorrect options plausible but clearly wrong? Is the correct answer unambiguous?\n4. For FILL_IN_BLANK: does the blank have exactly one correct answer? Is the hint helpful?\n5. For TRANSLATION: is the German translation grammatically correct and natural?\n6. Are exercises at A1 difficulty?\n7. Is there sufficient variety across the exercise types?\n8. Do exercises test concepts actually taught in the lesson?\n\nOutput a JSON object with this EXACT structure:\n{\n  \"overallAssessment\": \"string\",\n  \"exerciseCount\": number,\n  \"issues\": [\n    {\n      \"severity\": \"ERROR|WARNING|INFO\",\n      \"field\": \"string\",\n      \"message\": \"string\",\n      \"suggestion\": \"string\"\n    }\n  ],\n  \"recommendations\": [\"string\"]\n}"
        ));

        map.put(AgentType.PEDAGOGY_QA, new PromptRegistry.ResolvedPrompt(
                UUID.randomUUID(), "language.german.a1.qa.pedagogy", 1,
                "You are an expert language learning pedagogy reviewer evaluating the instructional quality of a German A1 lesson.\n\nFocus on pedagogical effectiveness: Does the lesson teach well? Is it learner-friendly?\n\nOutput ONLY valid JSON.",
                "Evaluate the pedagogical quality of this German A1 lesson.\n\nLesson content:\n{{lessonJson}}\n\nEvaluate:\n1. Learning objectives — are they clear, specific, and achievable?\n2. Explanation quality — does it support the learning objectives?\n3. Vocabulary introduction — is vocabulary introduced before it is used?\n4. Examples quality — do examples reinforce the grammar and vocabulary being taught?\n5. Lesson coherence — does the lesson have a logical learning progression?\n6. Learner-friendliness — is the tone encouraging and accessible?\n7. Translation support — is English translation consistently provided?\n8. Exercise alignment — do exercises meaningfully practice what the lesson taught?\n\nOutput a JSON object with this EXACT structure:\n{\n  \"overallAssessment\": \"string\",\n  \"issues\": [\n    {\n      \"severity\": \"ERROR|WARNING|INFO\",\n      \"field\": \"string\",\n      \"message\": \"string\",\n      \"suggestion\": \"string\"\n    }\n  ],\n  \"recommendations\": [\"string\"]\n}"
        ));

        return map;
    }

    @SuppressWarnings("unchecked")
    private QaResult parseQaResponse(String raw, String agentType, UUID lessonId) {
        String json = extractJson(raw);
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var root = mapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});

            String assessment = (String) root.getOrDefault("overallAssessment", "");
            List<java.util.Map<String, Object>> rawIssues =
                    (List<java.util.Map<String, Object>>) root.getOrDefault("issues", List.of());

            List<Issue> issues = new ArrayList<>();
            for (var ri : rawIssues) {
                String sev = (String) ri.getOrDefault("severity", "INFO");
                Issue.Severity severity;
                try { severity = Issue.Severity.valueOf(sev.toUpperCase()); }
                catch (IllegalArgumentException e) { severity = Issue.Severity.INFO; }
                issues.add(new Issue(agentType + "_issue", severity,
                        (String) ri.getOrDefault("field", ""),
                        (String) ri.getOrDefault("message", ""),
                        (String) ri.getOrDefault("suggestion", null), false));
            }

            List<String> recommendations = (List<String>) root.getOrDefault("recommendations", List.of());
            return new QaResult(assessment, issues, new ArrayList<>(recommendations));

        } catch (Exception e) {
            throw new IllegalStateException(
                    "E2E: failed to parse QA response for agent=" + agentType
                    + ". Snippet: " + raw.substring(0, Math.min(300, raw.length())), e);
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end   = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }
}
