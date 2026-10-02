package com.dsalearner.subscription.repository;

import com.dsalearner.subscription.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentEventRepository extends JpaRepository<PaymentEvent, UUID> {
    boolean existsByPlayOrderIdAndStatus(String playOrderId, PaymentEvent.EventStatus status);
    List<PaymentEvent> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
