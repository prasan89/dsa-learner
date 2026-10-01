package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaCityExpansionInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LangoaCityExpansionInstanceRepository extends JpaRepository<LangoaCityExpansionInstance, UUID> {
    List<LangoaCityExpansionInstance> findByCivilizationId(UUID civilizationId);
    boolean existsByCivilizationIdAndExpansionSlot(UUID civilizationId, int expansionSlot);
}
