package com.dsalearner.civilization.model.entity;

import com.dsalearner.civilization.domain.CurrencyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_currency_balances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaCurrencyBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency_type", nullable = false, length = 20)
    private CurrencyType currencyType;

    @Column(nullable = false)
    @Builder.Default
    private long balance = 0L;

    /** Max balance for this currency. NULL = unlimited (COINS, XP, GEMS, CIVILIZATION_POWER). */
    @Column(nullable = true)
    private Long capacity;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
