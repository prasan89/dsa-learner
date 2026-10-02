package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_building_production_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaBuildingProductionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "building_type", nullable = false, length = 50)
    private String buildingType;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType;

    @Column(name = "rate_per_hour", nullable = false)
    private int ratePerHour;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
