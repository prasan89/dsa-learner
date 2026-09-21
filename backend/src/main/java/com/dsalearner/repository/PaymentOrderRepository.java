package com.dsalearner.repository;

import com.dsalearner.model.entity.PaymentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {
    Optional<PaymentOrder> findByRazorpayOrderId(String razorpayOrderId);
}
