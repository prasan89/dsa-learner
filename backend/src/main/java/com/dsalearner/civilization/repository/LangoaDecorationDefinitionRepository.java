package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaDecorationDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LangoaDecorationDefinitionRepository extends JpaRepository<LangoaDecorationDefinition, UUID> {
    List<LangoaDecorationDefinition> findByActiveTrueOrderByDisplayOrder();
}
