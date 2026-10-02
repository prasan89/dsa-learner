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

    @Column(name = "cefr_level", nullable = false, length = 4)
    private String cefrLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_tier", nullable = false, length = 20)
    @Builder.Default
    private DifficultyTier difficultyTier = DifficultyTier.STANDARD;

    @Column(name = "xp_reward", nullable = false)
    @Builder.Default
    private long xpReward = 100L;

    @Column(name = "coin_reward", nullable = false)
    @Builder.Default
    private long coinReward = 50L;

    @Column(name = "food_reward", nullable = false)
    @Builder.Default
    private long foodReward = 10L;

    @Column(name = "material_reward", nullable = false)
    @Builder.Default
    private long materialReward = 5L;

    @Column(name = "civilization_power_reward", nullable = false)
    @Builder.Default
    private long civilizationPowerReward = 100L;

    @Column(name = "wood_reward", nullable = false)
    @Builder.Default
    private long woodReward = 5L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
