package com.dsalearner.civilization.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lang_coin_packages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangCoinPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 40)
    private String packageCode;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false)
    private long coinAmount;

    @Column(nullable = false)
    private int pricePaise;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(length = 100)
    private String playProductId;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
