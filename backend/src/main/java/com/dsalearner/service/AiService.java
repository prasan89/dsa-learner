package com.dsalearner.service;

import com.dsalearner.dto.request.AiMentorRequest;
import com.dsalearner.dto.response.AiMentorResponse;
import com.dsalearner.dto.response.AiReviewResponse;
import com.dsalearner.dto.response.PatternDetectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.base-url}")
    private String baseUrl;

    @Value("${app.ai.hint-model}")
    private String haikuModel;

    @Value("${app.ai.review-model}")
    private String sonnetModel;

    @Value("${app.ai.max-tokens}")
    private int maxTokens;

    private final WebClient.Builder webClientBuilder;

    public AiMentorResponse mentorChat(AiMentorRequest req) {
        String system = buildMentorSystemPrompt(req);
        String userMsg = buildMentorUserMessage(req);
        String raw = callClaude(haikuModel, system, userMsg);
        return parseMentorResponse(raw);
    }

    private String buildMentorSystemPrompt(AiMentorRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                You are a DSA mentor helping a student learn algorithms through practice.
                Your role: guide with questions and hints, NEVER give the answer directly.

                Rules:
                - Ask guiding questions that lead the student to discover the answer
                - Explain the WHY behind concepts, not just the WHAT
                - Reference the visualization they just completed when relevant
                - If code is present, point out specific lines with questions, not corrections
                - If tests failed, explain what the failure means conceptually
                - Keep responses concise (2-4 sentences max unless asked for more)
                - Respond in a warm, encouraging tone

                """);
        sb.append("Concept: ").append(req.conceptTitle()).append("\n");
        if (req.masteryLevel() != null && !req.masteryLevel().isEmpty()) {
            sb.append("Student mastery level: ").append(req.masteryLevel()).append("\n");
        }
        sb.append("Attempt #").append(req.attemptCount()).append(", hints used: ").append(req.hintsUsed()).append("\n");
        return sb.toString();
    }

    private String buildMentorUserMessage(AiMentorRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.currentCode() != null && !req.currentCode().isBlank()) {
            sb.append("Current code:\n```java\n").append(req.currentCode()).append("\n```\n\n");
        }
        if (req.compilerError() != null && !req.compilerError().isBlank()) {
            sb.append("Compiler error: ").append(req.compilerError()).append("\n\n");
        } else if (req.executionResult() != null && !req.executionResult().isBlank()) {
            sb.append("Execution result: ").append(req.executionResult()).append("\n\n");
        }
        if (req.previousMessages() != null) {
            for (AiMentorRequest.MessageEntry msg : req.previousMessages()) {
                sb.append(msg.role()).append(": ").append(msg.content()).append("\n");
            }
        }
        sb.append("Student: ").append(req.userMessage());
        return sb.toString();
    }

    private AiMentorResponse parseMentorResponse(String raw) {
        String type = "guidance";
        if (raw.contains("?")) type = "question";
        else if (raw.toLowerCase().contains("hint")) type = "hint";
        else if (raw.toLowerCase().contains("great") || raw.toLowerCase().contains("well done")) type = "encouragement";
        return new AiMentorResponse(raw.trim(), type);
    }

    private String callClaude(String model, String systemPrompt, String userMessage) {
        WebClient client = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", maxTokens,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        Map response = client.post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        return (String) content.get(0).get("text");
    }

    public AiReviewResponse reviewCode(String problemTitle, String problemDescription, String code) {
        String system = """
                You are a senior software engineer reviewing Java DSA solutions.
                Respond ONLY with a JSON object, no markdown, no explanation outside JSON.
                JSON format:
                {
                  "timeComplexity": "O(...)",
                  "spaceComplexity": "O(...)",
                  "strengths": "1-2 sentences on what is good",
                  "improvements": "1-2 sentences on what to improve",
                  "patternUsed": "name of the DSA pattern used",
                  "optimizedApproach": "1-2 sentences describing a better approach if any, else empty string"
                }
                """;

        String user = String.format("""
                Problem: %s
                Description: %s

                Student's Java solution:
                ```java
                %s
                ```
                """, problemTitle, problemDescription, code);

        String raw = callClaude(sonnetModel, system, user);
        return parseReviewResponse(raw);
    }

    public PatternDetectResponse detectPattern(String code) {
        String system = """
                You are a DSA expert. Given a Java code snippet, identify which algorithmic pattern it uses.
                Respond ONLY with a JSON object, no markdown, no explanation outside JSON.
                JSON format:
                {
                  "patternName": "full pattern name",
                  "patternSlug": "slug (e.g. two-pointers, sliding-window, hashing, binary-search, stack, linked-list, trees, heap, graph-bfs-dfs, arrays)",
                  "explanation": "1-2 sentences explaining why this pattern applies",
                  "confidence": "HIGH, MEDIUM, or LOW"
                }
                """;

        String raw = callClaude(haikuModel, system, "Identify the DSA pattern:\n```java\n" + code + "\n```");
        return parseDetectResponse(raw);
    }

    private AiReviewResponse parseReviewResponse(String raw) {
        try {
            String json = extractJson(raw);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, String> map = mapper.readValue(json, Map.class);
            return new AiReviewResponse(
                    map.getOrDefault("timeComplexity", ""),
                    map.getOrDefault("spaceComplexity", ""),
                    map.getOrDefault("strengths", ""),
                    map.getOrDefault("improvements", ""),
                    map.getOrDefault("patternUsed", ""),
                    map.getOrDefault("optimizedApproach", "")
            );
        } catch (Exception e) {
            return new AiReviewResponse("N/A", "N/A", raw, "", "", "");
        }
    }

    private PatternDetectResponse parseDetectResponse(String raw) {
        try {
            String json = extractJson(raw);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, String> map = mapper.readValue(json, Map.class);
            return new PatternDetectResponse(
                    map.getOrDefault("patternName", "Unknown"),
                    map.getOrDefault("patternSlug", ""),
                    map.getOrDefault("explanation", ""),
                    map.getOrDefault("confidence", "LOW")
            );
        } catch (Exception e) {
            return new PatternDetectResponse("Unknown", "", raw, "LOW");
        }
    }

    private String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) return text.substring(start, end + 1);
        return text;
    }
}
