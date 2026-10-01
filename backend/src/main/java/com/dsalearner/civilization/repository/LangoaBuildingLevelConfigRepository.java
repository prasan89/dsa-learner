package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaBuildingLevelConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaBuildingLevelConfigRepository extends JpaRepository<LangoaBuildingLevelConfig, UUID> {

    Optional<LangoaBuildingLevelConfig> findByBuildingTypeAndLevel(String buildingType, int level);

    List<LangoaBuildingLevelConfig> findByBuildingType(String buildingType);
}
