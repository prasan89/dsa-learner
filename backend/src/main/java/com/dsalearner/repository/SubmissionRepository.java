package com.dsalearner.repository;

import com.dsalearner.model.entity.Submission;
import com.dsalearner.model.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByUserIdAndProblemIdOrderBySubmittedAtDesc(UUID userId, UUID problemId);

    List<Submission> findByUserIdOrderBySubmittedAtDesc(UUID userId);

    boolean existsByUserIdAndProblemIdAndStatus(UUID userId, UUID problemId, SubmissionStatus status);

    @Query("SELECT COUNT(DISTINCT s.problem.id) FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    long countDistinctAcceptedProblemsByUserId(@Param("userId") UUID userId);
}
