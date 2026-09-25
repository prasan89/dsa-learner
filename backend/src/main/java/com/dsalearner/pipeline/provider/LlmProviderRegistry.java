package com.dsalearner.pipeline.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the correct LlmProvider by provider name.
 * All LlmProvider beans are auto-discovered at startup.
 * ModelRouter resolves the ModelConfig; this registry resolves the implementation.
 */
@Component
@Slf4j
public class LlmProviderRegistry {

    private final Map<String, LlmProvider> providers;

    public LlmProviderRegistry(List<LlmProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(LlmProvider::providerName, Function.identity()));
        log.info("LlmProviderRegistry loaded: {}", this.providers.keySet());
    }

    public LlmProvider get(String providerName) {
        LlmProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new LlmProviderException(
                    "No LlmProvider registered for provider: " + providerName, false);
        }
        return provider;
    }
}
