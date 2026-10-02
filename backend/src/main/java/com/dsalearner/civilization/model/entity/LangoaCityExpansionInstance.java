package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_city_expansion_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaCityExpansionInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "civilization_id", nullable = false)
    private UUID civilizationId;

    @Column(name = "expansion_slot", nullable = false)
    private int expansionSlot;

    @Column(name = "unlocked_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant unlockedAt = Instant.now();
}
