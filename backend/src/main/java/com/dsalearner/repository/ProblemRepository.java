package com.dsalearner.repository;

import com.dsalearner.model.entity.Problem;
import com.dsalearner.model.enums.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProblemRepository extends JpaRepository<Problem, UUID> {

    Optional<Problem> findBySlugAndActiveTrue(String slug);

    Page<Problem> findAllByActiveTrue(Pageable pageable);

    Page<Problem> findAllByActiveTrueAndDifficulty(Difficulty difficulty, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Problem p
        JOIN p.patterns pat
        WHERE p.active = true AND pat.id = :patternId
        """)
    Page<Problem> findAllByActiveTrueAndPatternId(@Param("patternId") UUID patternId, Pageable pageable);

    @Query("""
        SELECT DISTINCT p FROM Problem p
        JOIN p.patterns pat
        WHERE p.active = true AND pat.id = :patternId AND p.difficulty = :difficulty
        """)
    Page<Problem> findAllByActiveTrueAndPatternIdAndDifficulty(
            @Param("patternId") UUID patternId,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable);
}
