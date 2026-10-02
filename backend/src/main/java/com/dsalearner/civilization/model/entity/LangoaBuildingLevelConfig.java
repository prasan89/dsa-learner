package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "langoa_building_level_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaBuildingLevelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "building_type", nullable = false, length = 50)
    private String buildingType;

    @Column(nullable = false)
    private int level;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "coin_cost", nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(name = "food_cost", nullable = false)
    @Builder.Default
    private long foodCost = 0L;

    @Column(name = "material_cost", nullable = false)
    @Builder.Default
    private long materialCost = 0L;

    @Column(name = "xp_cost", nullable = false)
    @Builder.Default
    private long xpCost = 0L;

    @Column(name = "wood_cost", nullable = false)
    @Builder.Default
    private long woodCost = 0L;

    @Column(name = "required_lessons_completed", nullable = false)
    @Builder.Default
    private int requiredLessonsCompleted = 0;

    @Column(name = "required_xp", nullable = false)
    @Builder.Default
    private long requiredXp = 0L;

    @Column(name = "required_building_type", length = 50)
    private String requiredBuildingType;

    @Column(name = "required_building_level")
    private Integer requiredBuildingLevel;
}
