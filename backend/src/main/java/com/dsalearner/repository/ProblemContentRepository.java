package com.dsalearner.repository;

import com.dsalearner.model.entity.ProblemContent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProblemContentRepository extends JpaRepository<ProblemContent, UUID> {
    Optional<ProblemContent> findByProblemId(UUID problemId);
}
