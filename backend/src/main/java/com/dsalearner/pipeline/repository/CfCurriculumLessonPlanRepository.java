package com.dsalearner.pipeline.repository;

import com.dsalearner.pipeline.model.entity.CfCurriculumLessonPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CfCurriculumLessonPlanRepository extends JpaRepository<CfCurriculumLessonPlan, UUID> {

    Optional<CfCurriculumLessonPlan> findByStableRef(String stableRef);

    List<CfCurriculumLessonPlan> findByLevelIdOrderByPosition(UUID levelId);

    List<CfCurriculumLessonPlan> findByLevelIdAndPlanStatusOrderByPosition(UUID levelId, String planStatus);

    List<CfCurriculumLessonPlan> findByLevelIdAndPlanStatusInOrderByPosition(UUID levelId, List<String> statuses);

    List<CfCurriculumLessonPlan> findByCurriculumIdAndPlanStatusIn(UUID curriculumId, List<String> statuses);

    List<CfCurriculumLessonPlan> findByCurriculumId(UUID curriculumId);

    Optional<CfCurriculumLessonPlan> findByLessonId(UUID lessonId);

    long countByLevelId(UUID levelId);

    long countByLevelIdAndPlanStatusIn(UUID levelId, List<String> statuses);

    @Query("SELECT p FROM CfCurriculumLessonPlan p WHERE p.levelId = :levelId AND p.planStatus = 'PLANNED' ORDER BY p.position")
    List<CfCurriculumLessonPlan> findPlannedByLevel(@Param("levelId") UUID levelId);

    @Modifying
    @Query("UPDATE CfCurriculumLessonPlan p SET p.planStatus = :newStatus, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id AND p.planStatus = :expectedStatus")
    int compareAndSetStatus(@Param("id") UUID id,
                            @Param("expectedStatus") String expectedStatus,
                            @Param("newStatus") String newStatus);
}
