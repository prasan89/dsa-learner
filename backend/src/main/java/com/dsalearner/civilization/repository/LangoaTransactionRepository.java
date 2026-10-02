package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.model.entity.LangoaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LangoaTransactionRepository extends JpaRepository<LangoaTransaction, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<LangoaTransaction> findByUserIdAndLanguageCodeOrderByCreatedAtDesc(UUID userId, String languageCode);

    List<LangoaTransaction> findTop20ByUserIdAndLanguageCodeOrderByCreatedAtDesc(UUID userId, String languageCode);
}
