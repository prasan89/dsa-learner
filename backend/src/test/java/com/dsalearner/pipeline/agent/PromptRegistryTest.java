package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.exception.PromptNotFoundException;
import com.dsalearner.pipeline.model.entity.CfAgentPrompt;
import com.dsalearner.pipeline.repository.CfAgentPromptRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromptRegistryTest {

    @Mock CfAgentPromptRepository promptRepo;
    @InjectMocks PromptRegistry registry;

    @Test
    void resolvesActivePrompt() {
        CfAgentPrompt prompt = CfAgentPrompt.builder()
                .id(UUID.randomUUID())
                .promptKey("de_linguistic_qa")
                .version(3)
                .systemPrompt("You are a German linguistic QA agent.")
                .promptText("Evaluate the lesson for German A1.")
                .status("ACTIVE")
                .build();
        when(promptRepo.findTopByPromptKeyAndStatusOrderByVersionDesc("de_linguistic_qa", "ACTIVE"))
                .thenReturn(Optional.of(prompt));

        PromptRegistry.ResolvedPrompt resolved = registry.resolve("de_linguistic_qa");
        assertEquals(3, resolved.version());
        assertEquals("de_linguistic_qa", resolved.promptKey());
        assertNotNull(resolved.systemPrompt());
    }

    @Test
    void throwsWhenNoActivePrompt() {
        when(promptRepo.findTopByPromptKeyAndStatusOrderByVersionDesc(eq("missing_key"), eq("ACTIVE")))
                .thenReturn(Optional.empty());
        assertThrows(PromptNotFoundException.class, () -> registry.resolve("missing_key"));
    }

    @Test
    void resolvesSpecificVersion() {
        CfAgentPrompt prompt = CfAgentPrompt.builder()
                .id(UUID.randomUUID())
                .promptKey("cefr_qa")
                .version(2)
                .promptText("Check CEFR level.")
                .status("DEPRECATED")
                .build();
        when(promptRepo.findByPromptKeyAndVersion("cefr_qa", 2)).thenReturn(Optional.of(prompt));

        PromptRegistry.ResolvedPrompt resolved = registry.resolveVersion("cefr_qa", 2);
        assertEquals(2, resolved.version());
    }

    @Test
    void hasActivePromptReturnsFalseWhenMissing() {
        when(promptRepo.findTopByPromptKeyAndStatusOrderByVersionDesc(eq("no_key"), eq("ACTIVE")))
                .thenReturn(Optional.empty());
        assertFalse(registry.hasActivePrompt("no_key"));
    }
}
