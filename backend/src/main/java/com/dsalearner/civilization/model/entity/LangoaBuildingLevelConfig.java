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

    @Column(nullable = false, length = 50)
    private String buildingType;

    @Column(nullable = false)
    private int level;

    @Column(length = 100)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private long coinCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long foodCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long materialCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long xpCost = 0L;

    @Column(nullable = false)
    @Builder.Default
    private int requiredLessonsCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private long requiredXp = 0L;

    @Column(length = 50)
    private String requiredBuildingType;

    private Integer requiredBuildingLevel;
}
