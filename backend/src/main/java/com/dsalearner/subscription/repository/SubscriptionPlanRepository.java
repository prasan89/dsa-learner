package com.dsalearner.subscription.repository;

import com.dsalearner.subscription.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, String> {
    List<SubscriptionPlan> findByIsActiveTrue();
    Optional<SubscriptionPlan> findByPlanCodeAndIsActiveTrue(String planCode);
}
