package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaBuildingInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LangoaBuildingInstanceRepository extends JpaRepository<LangoaBuildingInstance, UUID> {

    List<LangoaBuildingInstance> findByCivilizationId(UUID civilizationId);

    boolean existsByCivilizationIdAndBuildingType(UUID civilizationId, String buildingType);
}
