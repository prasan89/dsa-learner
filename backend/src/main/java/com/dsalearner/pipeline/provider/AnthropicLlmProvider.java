package com.dsalearner.pipeline.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Messages API provider.
 * Reuses the same WebClient/API-key pattern as the existing AiService.
 * API key and base URL are read from application config — never hard-coded.
 */
@Component
@Slf4j
public class AnthropicLlmProvider implements LlmProvider {

    private final String apiKey;
    private final WebClient webClient;

    public AnthropicLlmProvider(
            @Value("${app.ai.api-key}") String apiKey,
            @Value("${app.ai.base-url:https://api.anthropic.com}") String baseUrl,
            WebClient.Builder webClientBuilder) {
        this.apiKey = apiKey;
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    public String providerName() { return "anthropic"; }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse generate(LlmRequest request) {
        Map<String, Object> body = Map.of(
                "model",      request.modelId(),
                "max_tokens", request.maxTokens(),
                "system",     request.systemPrompt(),
                "messages",   List.of(Map.of("role", "user", "content", request.userPrompt()))
        );

        try {
            Map<?, ?> response = webClient.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofMillis(request.timeoutMs()))
                    .block();

            if (response == null) {
                throw new LlmProviderException("Anthropic returned null response", true);
            }

            List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
            if (content == null || content.isEmpty()) {
                throw new LlmProviderException("Anthropic response missing content block", false);
            }
            String text = (String) content.get(0).get("text");

            Map<String, Object> usage = (Map<String, Object>) response.get("usage");
            int inputTokens  = usage != null ? ((Number) usage.get("input_tokens")).intValue()  : 0;
            int outputTokens = usage != null ? ((Number) usage.get("output_tokens")).intValue() : 0;

            log.debug("Anthropic response: model={} in={} out={}", request.modelId(), inputTokens, outputTokens);
            return new LlmResponse(text, inputTokens, outputTokens, request.modelId(), providerName());

        } catch (LlmProviderException e) {
            throw e;
        } catch (WebClientResponseException e) {
            // 4xx = non-retryable (bad request, auth, not found); 5xx = retryable
            boolean retryable = e.getStatusCode().is5xxServerError();
            throw new LlmProviderException(
                    "Anthropic HTTP %d: %s".formatted(e.getStatusCode().value(), e.getResponseBodyAsString()),
                    retryable, e);
        } catch (Exception e) {
            // Timeout, connection refused, etc. — all retryable
            throw new LlmProviderException("Anthropic provider error: " + e.getMessage(), true, e);
        }
    }
}
