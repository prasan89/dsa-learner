package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_building_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaBuildingInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID civilizationId;

    @Column(nullable = false, length = 50)
    private String buildingType;

    @Column(nullable = false)
    @Builder.Default
    private int currentLevel = 1;

    @Column(nullable = false)
    @Builder.Default
    private int positionX = 0;

    @Column(nullable = false)
    @Builder.Default
    private int positionY = 0;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String buildState = "BUILT";

    @Column(nullable = false)
    @Builder.Default
    private int rotationDeg = 0;

    @Column(nullable = false)
    @Builder.Default
    private int widthTiles = 1;

    @Column(nullable = false)
    @Builder.Default
    private int heightTiles = 1;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant builtAt = Instant.now();

    private Instant upgradedAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
