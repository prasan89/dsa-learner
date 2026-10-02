package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_decoration_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaDecorationInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "civilization_id", nullable = false)
    private UUID civilizationId;

    @Column(name = "decoration_type", nullable = false, length = 50)
    private String decorationType;

    @Column(name = "position_x", nullable = false)
    @Builder.Default
    private int positionX = 0;

    @Column(name = "position_y", nullable = false)
    @Builder.Default
    private int positionY = 0;

    @Column(name = "rotation_deg", nullable = false)
    @Builder.Default
    private int rotationDeg = 0;

    @Column(name = "placed_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant placedAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
