package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_decoration_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaDecorationDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "decoration_type", nullable = false, unique = true, length = 50)
    private String decorationType;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "asset_ref", length = 200)
    private String assetRef;

    @Column(name = "coin_cost", nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(name = "wood_cost", nullable = false)
    @Builder.Default
    private long woodCost = 0L;

    @Column(name = "required_civ_tier", nullable = false, length = 20)
    @Builder.Default
    private String requiredCivTier = "VILLAGE";

    @Column(name = "is_premium", nullable = false)
    @Builder.Default
    private boolean isPremium = false;

    @Column(name = "width_tiles", nullable = false)
    @Builder.Default
    private int widthTiles = 1;

    @Column(name = "height_tiles", nullable = false)
    @Builder.Default
    private int heightTiles = 1;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
