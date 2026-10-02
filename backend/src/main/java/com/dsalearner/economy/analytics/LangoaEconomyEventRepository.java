package com.dsalearner.economy.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LangoaEconomyEventRepository extends JpaRepository<LangoaEconomyEvent, UUID> {
    List<LangoaEconomyEvent> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByEventNameAndUserIdAndCreatedAtAfter(String eventName, UUID userId, Instant since);

    @Query("SELECT e.eventName, COUNT(e) FROM LangoaEconomyEvent e GROUP BY e.eventName ORDER BY COUNT(e) DESC")
    List<Object[]> countByEventName();
}
