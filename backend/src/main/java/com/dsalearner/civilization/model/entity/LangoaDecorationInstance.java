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

    @Column(nullable = false)
    private UUID civilizationId;

    @Column(nullable = false, length = 50)
    private String decorationType;

    @Column(nullable = false)
    @Builder.Default
    private int positionX = 0;

    @Column(nullable = false)
    @Builder.Default
    private int positionY = 0;

    @Column(nullable = false)
    @Builder.Default
    private int rotationDeg = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant placedAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
