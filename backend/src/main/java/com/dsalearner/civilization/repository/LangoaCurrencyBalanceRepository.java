package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.model.entity.LangoaCurrencyBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaCurrencyBalanceRepository extends JpaRepository<LangoaCurrencyBalance, UUID> {

    List<LangoaCurrencyBalance> findByUserIdAndLanguageCode(UUID userId, String languageCode);

    Optional<LangoaCurrencyBalance> findByUserIdAndLanguageCodeAndCurrencyType(
            UUID userId, String languageCode, CurrencyType currencyType);
}
