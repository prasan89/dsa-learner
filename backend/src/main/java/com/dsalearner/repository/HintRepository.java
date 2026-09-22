package com.dsalearner.repository;

import com.dsalearner.model.entity.Hint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HintRepository extends JpaRepository<Hint, UUID> {
    List<Hint> findByProblemIdOrderByLevel(UUID problemId);
    Optional<Hint> findByProblemIdAndLevel(UUID problemId, int level);
    int countByProblemId(UUID problemId);
}
