package com.dsalearner.civilization.model.entity;

import com.dsalearner.civilization.domain.DifficultyTier;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_reward_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaRewardDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 4)
    private String cefrLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DifficultyTier difficultyTier = DifficultyTier.STANDARD;

    @Column(nullable = false)
    @Builder.Default
    private long xpReward = 100L;

    @Column(nullable = false)
    @Builder.Default
    private long coinReward = 50L;

    @Column(nullable = false)
    @Builder.Default
    private long foodReward = 10L;

    @Column(nullable = false)
    @Builder.Default
    private long materialReward = 5L;

    @Column(nullable = false)
    @Builder.Default
    private long civilizationPowerReward = 100L;

    @Column(nullable = false)
    @Builder.Default
    private long woodReward = 5L;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
