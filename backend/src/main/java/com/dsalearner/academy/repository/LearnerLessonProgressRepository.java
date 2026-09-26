package com.dsalearner.academy.repository;

import com.dsalearner.academy.model.entity.LearnerLessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LearnerLessonProgressRepository extends JpaRepository<LearnerLessonProgress, UUID> {

    Optional<LearnerLessonProgress> findByUserIdAndLessonId(UUID userId, UUID lessonId);

    List<LearnerLessonProgress> findByUserId(UUID userId);

    List<LearnerLessonProgress> findByUserIdAndStatus(UUID userId, String status);

    @Query("SELECT COUNT(p) FROM LearnerLessonProgress p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    long countCompletedByUser(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            UPDATE LearnerLessonProgress p
            SET p.status = :newStatus,
                p.stepIndex = :stepIndex,
                p.lastInteractionAt = :now,
                p.updatedAt = :now
            WHERE p.id = :id
              AND p.status = :expectedStatus
            """)
    int compareAndSetStatus(
            @Param("id") UUID id,
            @Param("expectedStatus") String expectedStatus,
            @Param("newStatus") String newStatus,
            @Param("stepIndex") int stepIndex,
            @Param("now") Instant now);
}
