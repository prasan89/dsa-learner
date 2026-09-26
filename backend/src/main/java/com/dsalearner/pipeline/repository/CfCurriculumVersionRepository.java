package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumVersionRepository extends JpaRepository<CfCurriculumVersion, UUID> {

    List<CfCurriculumVersion> findByCurriculumIdOrderByVersionDesc(UUID curriculumId);

    Optional<CfCurriculumVersion> findByCurriculumIdAndVersion(UUID curriculumId, int version);

    Optional<CfCurriculumVersion> findFirstByCurriculumIdOrderByVersionDesc(UUID curriculumId);
}
