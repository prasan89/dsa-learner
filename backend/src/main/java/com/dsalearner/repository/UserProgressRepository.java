package com.dsalearner.repository;

import com.dsalearner.model.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

    Optional<UserProgress> findByUserIdAndProblemId(UUID userId, UUID problemId);

    boolean existsByUserIdAndProblemIdAndSolvedTrue(UUID userId, UUID problemId);

    @Query("SELECT up.problem.id FROM UserProgress up WHERE up.user.id = :userId AND up.solved = true")
    Set<UUID> findSolvedProblemIdsByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT up FROM UserProgress up
        JOIN FETCH up.problem p
        WHERE up.user.id = :userId
        ORDER BY up.lastAttemptAt DESC
        """)
    List<UserProgress> findRecentByUserId(@Param("userId") UUID userId);

    long countByUserIdAndSolvedTrue(UUID userId);

    @Query("""
        SELECT COUNT(up) FROM UserProgress up
        JOIN up.problem p
        WHERE up.user.id = :userId AND up.solved = true AND p.difficulty = :difficulty
        """)
    long countSolvedByUserIdAndDifficulty(@Param("userId") UUID userId,
                                          @Param("difficulty") com.dsalearner.model.enums.Difficulty difficulty);

    @Query("""
        SELECT up FROM UserProgress up
        JOIN FETCH up.problem p
        JOIN p.patterns pat
        WHERE up.user.id = :userId AND pat.id = :patternId
        """)
    List<UserProgress> findByUserIdAndPatternId(@Param("userId") UUID userId,
                                                @Param("patternId") UUID patternId);

    @Query("""
        SELECT COUNT(p) FROM Problem p JOIN p.patterns pat WHERE pat.id = :patternId AND p.active = true
        """)
    long countProblemsByPatternId(@Param("patternId") UUID patternId);
}
