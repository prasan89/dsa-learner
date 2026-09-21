package com.dsalearner.repository;

import com.dsalearner.model.entity.PatternMastery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatternMasteryRepository extends JpaRepository<PatternMastery, UUID> {
    Optional<PatternMastery> findByUserIdAndPatternId(UUID userId, UUID patternId);
    List<PatternMastery> findByUserId(UUID userId);
}
