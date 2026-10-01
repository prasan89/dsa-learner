package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.domain.DifficultyTier;
import com.dsalearner.civilization.model.entity.LangoaRewardDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LangoaRewardDefinitionRepository extends JpaRepository<LangoaRewardDefinition, UUID> {

    Optional<LangoaRewardDefinition> findByCefrLevelAndDifficultyTier(String cefrLevel, DifficultyTier difficultyTier);
}
