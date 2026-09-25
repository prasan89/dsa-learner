package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.exception.PromptNotFoundException;
import com.dsalearner.pipeline.model.entity.CfAgentPrompt;
import com.dsalearner.pipeline.repository.CfAgentPromptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the active prompt for an agent execution.
 * Every agent run must know exactly which prompt version produced its result.
 */
@Component
@RequiredArgsConstructor
public class PromptRegistry {

    private final CfAgentPromptRepository promptRepository;

    /**
     * Returns the currently ACTIVE prompt for a given key.
     * For language-specific agents, pass the full key: e.g. 'de_linguistic_qa'.
     * For domain-wide agents, pass the agent type: e.g. 'cefr_qa'.
     */
    public ResolvedPrompt resolve(String promptKey) {
        CfAgentPrompt prompt = promptRepository.findTopByPromptKeyAndStatusOrderByVersionDesc(promptKey, "ACTIVE")
                .orElseThrow(() -> new PromptNotFoundException(
                        "No active prompt for key: " + promptKey));
        return new ResolvedPrompt(prompt.getId(), prompt.getPromptKey(), prompt.getVersion(),
                prompt.getSystemPrompt(), prompt.getPromptText());
    }

    /**
     * Returns a specific version of a prompt.
     */
    public ResolvedPrompt resolveVersion(String promptKey, int version) {
        CfAgentPrompt prompt = promptRepository.findByPromptKeyAndVersion(promptKey, version)
                .orElseThrow(() -> new PromptNotFoundException(
                        "Prompt not found: key=%s version=%d".formatted(promptKey, version)));
        return new ResolvedPrompt(prompt.getId(), prompt.getPromptKey(), prompt.getVersion(),
                prompt.getSystemPrompt(), prompt.getPromptText());
    }

    public boolean hasActivePrompt(String promptKey) {
        return promptRepository.findTopByPromptKeyAndStatusOrderByVersionDesc(promptKey, "ACTIVE").isPresent();
    }

    public record ResolvedPrompt(
            UUID id,
            String promptKey,
            int version,
            String systemPrompt,
            String promptText
    ) {}
}
