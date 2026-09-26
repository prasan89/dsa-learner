package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumUnit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CfCurriculumUnitRepository extends JpaRepository<CfCurriculumUnit, UUID> {

    List<CfCurriculumUnit> findByLevelIdOrderByOrdinal(UUID levelId);

    List<CfCurriculumUnit> findByCurriculumIdOrderByLevelIdAscOrdinalAsc(UUID curriculumId);
}
