package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumDependencyRepository extends JpaRepository<CfCurriculumDependency, UUID> {

    List<CfCurriculumDependency> findByLessonPlanId(UUID lessonPlanId);

    List<CfCurriculumDependency> findByRequiredPlanId(UUID requiredPlanId);

    List<CfCurriculumDependency> findByCurriculumId(UUID curriculumId);

    List<CfCurriculumDependency> findByLessonPlanIdAndDependencyType(UUID lessonPlanId, String dependencyType);

    boolean existsByLessonPlanIdAndRequiredPlanId(UUID lessonPlanId, UUID requiredPlanId);

    Optional<CfCurriculumDependency> findByLessonPlanIdAndRequiredPlanId(UUID lessonPlanId, UUID requiredPlanId);

    List<CfCurriculumDependency> findAllByLessonPlanIdIn(Collection<UUID> lessonPlanIds);

    void deleteAllByCurriculumId(UUID curriculumId);
}
