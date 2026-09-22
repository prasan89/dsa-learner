package com.dsalearner.repository;

import com.dsalearner.model.entity.ProblemFollowup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProblemFollowupRepository extends JpaRepository<ProblemFollowup, UUID> {
    List<ProblemFollowup> findByProblemIdOrderBySortOrderAsc(UUID problemId);
}
