package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfAiModelConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CfAiModelConfigRepository extends JpaRepository<CfAiModelConfig, UUID> {
    Optional<CfAiModelConfig> findByConfigKey(String configKey);
}
