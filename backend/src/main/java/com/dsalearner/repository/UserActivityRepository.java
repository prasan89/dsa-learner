package com.dsalearner.repository;

import com.dsalearner.model.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UserActivityRepository extends JpaRepository<UserActivity, UUID> {

    boolean existsByUserIdAndActivityDate(UUID userId, LocalDate date);

    @Query("""
        SELECT ua.activityDate FROM UserActivity ua
        WHERE ua.userId = :userId
        ORDER BY ua.activityDate DESC
        """)
    List<LocalDate> findDatesByUserIdOrderByDateDesc(@Param("userId") UUID userId);
}
