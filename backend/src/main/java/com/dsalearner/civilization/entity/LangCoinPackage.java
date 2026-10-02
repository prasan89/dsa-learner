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

    @Column(name = "package_code", nullable = false, unique = true, length = 40)
    private String packageCode;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "coin_amount", nullable = false)
    private long coinAmount;

    @Column(name = "price_paise", nullable = false)
    private int pricePaise;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "play_product_id", length = 100)
    private String playProductId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
