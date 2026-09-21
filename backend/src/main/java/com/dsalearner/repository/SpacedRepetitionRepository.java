package com.dsalearner.repository;

import com.dsalearner.model.entity.SpacedRepetitionReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpacedRepetitionRepository extends JpaRepository<SpacedRepetitionReview, UUID> {

    @Query("SELECT r FROM SpacedRepetitionReview r JOIN FETCH r.problem p " +
           "WHERE r.user.id = :userId AND r.dueDate <= :today AND r.completedAt IS NULL " +
           "ORDER BY r.dueDate ASC")
    List<SpacedRepetitionReview> findDueReviews(UUID userId, LocalDate today);

    Optional<SpacedRepetitionReview> findTopByUserIdAndProblemIdOrderByCreatedAtDesc(UUID userId, UUID problemId);

    long countByUserIdAndDueDateAndCompletedAtIsNull(UUID userId, LocalDate dueDate);
}
