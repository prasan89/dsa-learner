package com.dsalearner.academy.repository;

import com.dsalearner.academy.model.entity.LearnerLevelProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerLevelProgressRepository extends JpaRepository<LearnerLevelProgress, UUID> {

    Optional<LearnerLevelProgress> findByUserIdAndCurriculumIdAndCefrLevel(
            UUID userId, UUID curriculumId, String cefrLevel);

    List<LearnerLevelProgress> findByUserIdAndCurriculumIdOrderByCefrLevel(
            UUID userId, UUID curriculumId);

    List<LearnerLevelProgress> findByUserId(UUID userId);

    @Query("""
            SELECT p FROM LearnerLevelProgress p
            WHERE p.userId = :userId
              AND p.curriculumId = :curriculumId
              AND p.status = 'IN_PROGRESS'
            """)
    List<LearnerLevelProgress> findActiveByUserAndCurriculum(
            @Param("userId") UUID userId,
            @Param("curriculumId") UUID curriculumId);
}
