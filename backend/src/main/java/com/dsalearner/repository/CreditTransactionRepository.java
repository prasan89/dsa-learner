package com.dsalearner.repository;

import com.dsalearner.model.entity.CreditTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
