package com.dsalearner.pipeline.provider;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that AnthropicLlmProvider constructs the correct request URI
 * from the configured base URL.
 *
 * The provider always appends /v1/messages to the configured base URL.
 * Therefore:
 *   base-url = https://api.anthropic.com  → https://api.anthropic.com/v1/messages
 *   base-url = http://localhost:6655/anthropic → http://localhost:6655/anthropic/v1/messages
 *
 * This test captures the outgoing request URI without making a real HTTP call.
 */
class AnthropicUrlConstructionTest {

    @Test
    void productionBaseUrlConstructsCorrectUri() {
        URI captured = captureRequestUri("https://api.anthropic.com");
        assertEquals("https://api.anthropic.com/v1/messages", captured.toString());
    }

    @Test
    void localProxyBaseUrlConstructsCorrectUri() {
        URI captured = captureRequestUri("http://localhost:6655/anthropic");
        assertEquals("http://localhost:6655/anthropic/v1/messages", captured.toString());
    }

    @Test
    void localProxyRootBaseUrlProducesWrongUri() {
        // Documents the WRONG configuration: /v1/messages ends up at the wrong path.
        URI captured = captureRequestUri("http://localhost:6655");
        assertEquals("http://localhost:6655/v1/messages", captured.toString(),
                "Using the proxy root without /anthropic path prefix produces the wrong URI");
    }

    /**
     * Builds an AnthropicLlmProvider with the given base URL and a capturing WebClient.
     * Triggers one generate() call (which will fail with a fake response) and returns
     * the URI that was sent to the WebClient.
     */
    private URI captureRequestUri(String baseUrl) {
        AtomicReference<URI> capturedUri = new AtomicReference<>();

        ExchangeFilterFunction capturingFilter = ExchangeFilterFunction.ofRequestProcessor(req -> {
            capturedUri.set(req.url());
            // Return a fake 200 OK with minimal Anthropic-shaped body so the provider parses it.
            return Mono.just(req);
        });

        // Build a WebClient that records the URI then returns a mock response.
        WebClient.Builder builder = WebClient.builder()
                .filter(capturingFilter)
                .exchangeFunction(request -> {
                    capturedUri.set(request.url());
                    // Return a minimal Anthropic-shaped response so the provider does not throw.
                    String body = """
                            {"content":[{"text":"ok"}],"usage":{"input_tokens":1,"output_tokens":1}}
                            """;
                    return Mono.just(ClientResponse.create(org.springframework.http.HttpStatus.OK)
                            .header("Content-Type", "application/json")
                            .body(body)
                            .build());
                });

        AnthropicLlmProvider provider = new AnthropicLlmProvider("test-api-key", baseUrl, builder);
        LlmRequest req = new LlmRequest("claude-sonnet-4-6", "system", "user", 100, 0.3, 5000);
        provider.generate(req); // Fires the request; response is parsed but we only care about the URI.

        assertNotNull(capturedUri.get(), "URI must be captured");
        return capturedUri.get();
    }
}
