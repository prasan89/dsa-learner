package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_city_expansion_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaCityExpansionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private int expansionSlot;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private int gridXOffset = 0;

    @Column(nullable = false)
    @Builder.Default
    private int gridYOffset = 0;

    @Column(nullable = false)
    @Builder.Default
    private int gridWidth = 10;

    @Column(nullable = false)
    @Builder.Default
    private int gridHeight = 10;

    @Column(nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long woodCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private int requiredLessons = 0;

    @Column(nullable = false)
    @Builder.Default
    private long requiredXp = 0L;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String requiredCivTier = "VILLAGE";

    @Column(nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
