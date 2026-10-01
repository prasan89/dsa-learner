package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaDecorationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LangoaDecorationInstanceRepository extends JpaRepository<LangoaDecorationInstance, UUID> {
    List<LangoaDecorationInstance> findByCivilizationId(UUID civilizationId);
}
