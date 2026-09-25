package com.dsalearner.pipeline.agent;

import com.dsalearner.pipeline.exception.ModelConfigNotFoundException;
import com.dsalearner.pipeline.model.entity.CfAiModelConfig;
import com.dsalearner.pipeline.repository.CfAiModelConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves ModelConfig for an agent execution.
 * Agents never reference providers or model IDs directly.
 *
 * Resolution order:
 *   1. agentType + domainCode + languageCode (most specific)
 *   2. agentType + domainCode
 *   3. agentType
 *   4. configKey literal fallback
 */
@Component
@RequiredArgsConstructor
public class ModelRouter {

    private final CfAiModelConfigRepository configRepository;

    public ModelConfig resolve(String agentType, String domainCode, String languageCode) {
        // Try most specific → least specific
        if (languageCode != null) {
            Optional<CfAiModelConfig> specific = configRepository.findByConfigKey(
                    agentType + "_" + domainCode + "_" + languageCode);
            if (specific.isPresent()) return toModelConfig(specific.get());
        }

        Optional<CfAiModelConfig> domainSpecific = configRepository.findByConfigKey(
                agentType + "_" + domainCode);
        if (domainSpecific.isPresent()) return toModelConfig(domainSpecific.get());

        Optional<CfAiModelConfig> agentDefault = configRepository.findByConfigKey(agentType);
        if (agentDefault.isPresent()) return toModelConfig(agentDefault.get());

        // Domain-level defaults
        String defaultKey = switch (agentType) {
            case AgentType.CEFR_QA, AgentType.PEDAGOGY_QA,
                 AgentType.EXERCISE_QA, AgentType.CONSISTENCY_QA -> "haiku_qa_v1";
            default -> "sonnet_gen_v1";
        };

        return configRepository.findByConfigKey(defaultKey)
                .map(this::toModelConfig)
                .orElseThrow(() -> new ModelConfigNotFoundException(
                        "No model config found for agent=%s domain=%s lang=%s"
                                .formatted(agentType, domainCode, languageCode)));
    }

    public ModelConfig resolveByKey(String configKey) {
        return configRepository.findByConfigKey(configKey)
                .map(this::toModelConfig)
                .orElseThrow(() -> new ModelConfigNotFoundException("Model config not found: " + configKey));
    }

    private ModelConfig toModelConfig(CfAiModelConfig e) {
        return new ModelConfig(
                e.getConfigKey(), e.getProvider(), e.getModelId(),
                e.getTemperature().doubleValue(), e.getMaxTokens(), e.getTimeoutMs(),
                e.getCostPer1kInputUsd(), e.getCostPer1kOutputUsd()
        );
    }
}
