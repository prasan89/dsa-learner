package com.dsalearner.civilization.repository;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.model.entity.LangoaCurrencyBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LangoaCurrencyBalanceRepository extends JpaRepository<LangoaCurrencyBalance, UUID> {

    List<LangoaCurrencyBalance> findByUserIdAndLanguageCode(UUID userId, String languageCode);

    Optional<LangoaCurrencyBalance> findByUserIdAndLanguageCodeAndCurrencyType(
            UUID userId, String languageCode, CurrencyType currencyType);

    /**
     * SELECT FOR UPDATE — use when about to modify the balance to prevent lost-update races.
     * Must be called inside a @Transactional method.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM LangoaCurrencyBalance b WHERE b.userId = :userId AND b.languageCode = :languageCode AND b.currencyType = :currencyType")
    Optional<LangoaCurrencyBalance> findForUpdate(
            @Param("userId") UUID userId,
            @Param("languageCode") String languageCode,
            @Param("currencyType") CurrencyType currencyType);
}
