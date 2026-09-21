package com.dsalearner.repository;

import com.dsalearner.model.entity.UserHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserHintRepository extends JpaRepository<UserHint, UUID> {
    Optional<UserHint> findByUserIdAndHintId(UUID userId, UUID hintId);
    boolean existsByUserIdAndHintId(UUID userId, UUID hintId);

    @Query("SELECT uh.hint.id FROM UserHint uh WHERE uh.user.id = :userId")
    List<UUID> findUnlockedHintIdsByUserId(UUID userId);
}
