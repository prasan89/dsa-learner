package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfAgentPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CfAgentPromptRepository extends JpaRepository<CfAgentPrompt, UUID> {
    Optional<CfAgentPrompt> findTopByPromptKeyAndStatusOrderByVersionDesc(String promptKey, String status);
    Optional<CfAgentPrompt> findByPromptKeyAndVersion(String promptKey, int version);
}
