package com.dsalearner.economy.antiabuse;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LangoaSuspiciousActivityRepository extends JpaRepository<LangoaSuspiciousActivity, UUID> {
    List<LangoaSuspiciousActivity> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByUserIdAndCreatedAtAfter(UUID userId, Instant since);
    List<LangoaSuspiciousActivity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
