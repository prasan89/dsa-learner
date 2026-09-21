package com.dsalearner.service;

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
