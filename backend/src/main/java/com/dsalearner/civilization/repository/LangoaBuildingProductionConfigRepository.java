package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaBuildingProductionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LangoaBuildingProductionConfigRepository
        extends JpaRepository<LangoaBuildingProductionConfig, UUID> {

    List<LangoaBuildingProductionConfig> findByBuildingTypeAndLevel(String buildingType, int level);
}
