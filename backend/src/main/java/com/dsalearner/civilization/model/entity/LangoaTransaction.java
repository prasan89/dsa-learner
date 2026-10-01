package com.dsalearner.civilization.model.entity;

import com.dsalearner.civilization.domain.CurrencyType;
import com.dsalearner.civilization.domain.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CurrencyType currencyType;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false)
    private long balanceAfter;

    @Column(length = 100)
    private String sourceReference;

    @Column(length = 100, unique = true)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
