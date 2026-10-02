package com.dsalearner.subscription.repository;

import com.dsalearner.subscription.entity.LangSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LangSubscriptionRepository extends JpaRepository<LangSubscription, UUID> {
    Optional<LangSubscription> findByUserId(UUID userId);
    List<LangSubscription> findByStatusAndCurrentPeriodEndBefore(LangSubscription.Status status, Instant cutoff);
}
