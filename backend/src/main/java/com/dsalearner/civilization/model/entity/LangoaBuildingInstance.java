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

    @Column(name = "civilization_id", nullable = false)
    private UUID civilizationId;

    @Column(name = "building_type", nullable = false, length = 50)
    private String buildingType;

    @Column(name = "current_level", nullable = false)
    @Builder.Default
    private int currentLevel = 1;

    @Column(name = "position_x", nullable = false)
    @Builder.Default
    private int positionX = 0;

    @Column(name = "position_y", nullable = false)
    @Builder.Default
    private int positionY = 0;

    @Column(name = "build_state", nullable = false, length = 20)
    @Builder.Default
    private String buildState = "BUILT";

    @Column(name = "rotation_deg", nullable = false)
    @Builder.Default
    private int rotationDeg = 0;

    @Column(name = "width_tiles", nullable = false)
    @Builder.Default
    private int widthTiles = 1;

    @Column(name = "height_tiles", nullable = false)
    @Builder.Default
    private int heightTiles = 1;

    @Column(name = "built_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant builtAt = Instant.now();

    @Column(name = "upgraded_at")
    private Instant upgradedAt;

    /** Timestamp of last production tick for this building — drives lazy production calc. */
    @Column(name = "last_production_at")
    @Builder.Default
    private Instant lastProductionAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
