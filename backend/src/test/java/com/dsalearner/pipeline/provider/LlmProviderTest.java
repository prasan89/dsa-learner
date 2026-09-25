package com.dsalearner.pipeline.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LlmProviderTest {

    private final MockLlmProvider mockProvider = new MockLlmProvider();

    @Test
    void mockProviderReturnsRealisticResponse() {
        LlmRequest req = new LlmRequest("mock-model", "system", "user", 1000, 0.3, 5000);
        LlmResponse response = mockProvider.generate(req);

        assertNotNull(response.text());
        assertFalse(response.text().isBlank());
        assertEquals("mock", response.provider());
        assertTrue(response.inputTokens() > 0);
        assertTrue(response.outputTokens() > 0);
    }

    @Test
    void mockProviderResponseContainsValidJson() throws Exception {
        LlmRequest req = new LlmRequest("mock-model", "system", "user", 1000, 0.3, 5000);
        LlmResponse response = mockProvider.generate(req);

        // Extract JSON from response
        String text = response.text();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        assertTrue(start >= 0 && end > start, "Response must contain a JSON object");

        String json = text.substring(start, end + 1);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        java.util.Map<?, ?> parsed = mapper.readValue(json, java.util.Map.class);

        assertTrue(parsed.containsKey("metadata"),   "metadata required");
        assertTrue(parsed.containsKey("vocabulary"),  "vocabulary required");
        assertTrue(parsed.containsKey("grammar"),     "grammar required");
        assertTrue(parsed.containsKey("exercises"),   "exercises required");
        assertTrue(parsed.containsKey("objectives"),  "objectives required");
    }

    @Test
    void llmProviderRegistryResolvesProviderByName() {
        LlmProviderRegistry registry = new LlmProviderRegistry(java.util.List.of(mockProvider));
        LlmProvider resolved = registry.get("mock");
        assertSame(mockProvider, resolved);
    }

    @Test
    void llmProviderRegistryThrowsForUnknownProvider() {
        LlmProviderRegistry registry = new LlmProviderRegistry(java.util.List.of(mockProvider));
        assertThrows(LlmProviderException.class, () -> registry.get("openai"));
    }

    @Test
    void llmProviderExceptionRetryableFlagIsPreserved() {
        LlmProviderException retryable    = new LlmProviderException("timeout",    true);
        LlmProviderException notRetryable = new LlmProviderException("bad request", false);

        assertTrue(retryable.isRetryable());
        assertFalse(notRetryable.isRetryable());
    }
}
