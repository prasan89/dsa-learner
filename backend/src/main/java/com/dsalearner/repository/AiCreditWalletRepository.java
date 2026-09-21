package com.dsalearner.repository;

import com.dsalearner.model.entity.AiCreditWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AiCreditWalletRepository extends JpaRepository<AiCreditWallet, UUID> {
    Optional<AiCreditWallet> findByUserId(UUID userId);
}
