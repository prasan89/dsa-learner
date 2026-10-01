package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaQuestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LangoaQuestDefinitionRepository extends JpaRepository<LangoaQuestDefinition, UUID> {
    List<LangoaQuestDefinition> findByActiveTrueAndQuestTypeIn(List<String> questTypes);
    List<LangoaQuestDefinition> findByTargetTypeAndActiveTrue(String targetType);
}
