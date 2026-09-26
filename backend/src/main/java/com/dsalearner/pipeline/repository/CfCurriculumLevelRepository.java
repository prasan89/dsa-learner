package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumLevelRepository extends JpaRepository<CfCurriculumLevel, UUID> {

    List<CfCurriculumLevel> findByCurriculumIdOrderByOrdinal(UUID curriculumId);

    Optional<CfCurriculumLevel> findByCurriculumIdAndCefrLevel(UUID curriculumId, String cefrLevel);

    Optional<CfCurriculumLevel> findByCurriculumIdAndOrdinal(UUID curriculumId, int ordinal);

    List<CfCurriculumLevel> findByCurriculumIdAndLevelStatusIn(UUID curriculumId, List<String> statuses);
}
