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

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CurrencyType currencyType;

    @Column(nullable = false)
    @Builder.Default
    private long balance = 0L;

    @UpdateTimestamp
    private Instant updatedAt;
}
