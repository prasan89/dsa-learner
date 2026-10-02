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

    @Column(name = "expansion_slot", nullable = false, unique = true)
    private int expansionSlot;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "grid_x_offset", nullable = false)
    @Builder.Default
    private int gridXOffset = 0;

    @Column(name = "grid_y_offset", nullable = false)
    @Builder.Default
    private int gridYOffset = 0;

    @Column(name = "grid_width", nullable = false)
    @Builder.Default
    private int gridWidth = 10;

    @Column(name = "grid_height", nullable = false)
    @Builder.Default
    private int gridHeight = 10;

    @Column(name = "coin_cost", nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(name = "wood_cost", nullable = false)
    @Builder.Default
    private long woodCost = 0L;

    @Column(name = "required_lessons", nullable = false)
    @Builder.Default
    private int requiredLessons = 0;

    @Column(name = "required_xp", nullable = false)
    @Builder.Default
    private long requiredXp = 0L;

    @Column(name = "required_civ_tier", nullable = false, length = 20)
    @Builder.Default
    private String requiredCivTier = "VILLAGE";

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
