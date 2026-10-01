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

    @Column(nullable = false, unique = true, length = 50)
    private String decorationType;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String assetRef;

    @Column(nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long woodCost = 0L;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String requiredCivTier = "VILLAGE";

    @Column(nullable = false)
    @Builder.Default
    private boolean isPremium = false;

    @Column(nullable = false)
    @Builder.Default
    private int widthTiles = 1;

    @Column(nullable = false)
    @Builder.Default
    private int heightTiles = 1;

    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
