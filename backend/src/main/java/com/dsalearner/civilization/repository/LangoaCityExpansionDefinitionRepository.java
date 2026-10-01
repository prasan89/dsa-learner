package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaCityExpansionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaCityExpansionDefinitionRepository extends JpaRepository<LangoaCityExpansionDefinition, UUID> {
    List<LangoaCityExpansionDefinition> findAllByOrderByDisplayOrder();
    Optional<LangoaCityExpansionDefinition> findByExpansionSlot(int slot);
}
