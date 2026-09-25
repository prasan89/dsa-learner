package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.exception.ModelConfigNotFoundException;
import com.dsalearner.pipeline.model.entity.CfAiModelConfig;
import com.dsalearner.pipeline.repository.CfAiModelConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ModelRouterTest {

    @Mock CfAiModelConfigRepository configRepo;
    @InjectMocks ModelRouter router;

    @Test
    void resolvesSpecificKey_agentTypeDomainLanguage() {
        CfAiModelConfig cfg = mockConfig("haiku_qa_v1");
        when(configRepo.findByConfigKey("linguistic_qa_language_de")).thenReturn(Optional.of(cfg));

        ModelConfig result = router.resolve("linguistic_qa", "language", "de");
        assertEquals("haiku_qa_v1", result.configKey());
    }

    @Test
    void fallsBackToDomainKey() {
        when(configRepo.findByConfigKey("cefr_qa_language_de")).thenReturn(Optional.empty());
        when(configRepo.findByConfigKey("cefr_qa_language")).thenReturn(Optional.empty());
        when(configRepo.findByConfigKey("cefr_qa")).thenReturn(Optional.empty());
        CfAiModelConfig cfg = mockConfig("haiku_qa_v1");
        when(configRepo.findByConfigKey("haiku_qa_v1")).thenReturn(Optional.of(cfg));

        ModelConfig result = router.resolve("cefr_qa", "language", "de");
        assertEquals("haiku_qa_v1", result.configKey());
    }

    @Test
    void throwsWhenNoConfigFound() {
        when(configRepo.findByConfigKey(any())).thenReturn(Optional.empty());
        assertThrows(ModelConfigNotFoundException.class,
                () -> router.resolve("unknown_agent", "language", null));
    }

    @Test
    void resolvesWithoutLanguage() {
        CfAiModelConfig cfg = mockConfig("sonnet_gen_v1");
        when(configRepo.findByConfigKey("content_generator_language")).thenReturn(Optional.empty());
        when(configRepo.findByConfigKey("content_generator")).thenReturn(Optional.empty());
        when(configRepo.findByConfigKey("sonnet_gen_v1")).thenReturn(Optional.of(cfg));

        ModelConfig result = router.resolve("content_generator", "language", null);
        assertEquals("sonnet_gen_v1", result.configKey());
    }

    private CfAiModelConfig mockConfig(String key) {
        return CfAiModelConfig.builder()
                .configKey(key)
                .provider("anthropic")
                .modelId("claude-haiku-4-5")
                .temperature(new BigDecimal("0.10"))
                .maxTokens(2048)
                .timeoutMs(15000)
                .costPer1kInputUsd(new BigDecimal("0.000250"))
                .costPer1kOutputUsd(new BigDecimal("0.001250"))
                .build();
    }
}
