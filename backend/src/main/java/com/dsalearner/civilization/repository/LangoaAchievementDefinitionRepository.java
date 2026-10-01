package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaAchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LangoaAchievementDefinitionRepository extends JpaRepository<LangoaAchievementDefinition, UUID> {
    List<LangoaAchievementDefinition> findByActiveTrueAndTriggerType(String triggerType);
    List<LangoaAchievementDefinition> findByActiveTrue();
}
