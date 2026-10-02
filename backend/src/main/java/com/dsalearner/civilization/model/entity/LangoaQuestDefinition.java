package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_quest_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaQuestDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "quest_key", nullable = false, unique = true, length = 100)
    private String questKey;

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "quest_type", nullable = false, length = 30)
    @Builder.Default
    private String questType = "DAILY";

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_count", nullable = false)
    @Builder.Default
    private int targetCount = 1;

    @Column(name = "xp_reward", nullable = false)
    @Builder.Default
    private long xpReward = 0L;

    @Column(name = "coin_reward", nullable = false)
    @Builder.Default
    private long coinReward = 0L;

    @Column(name = "food_reward", nullable = false)
    @Builder.Default
    private long foodReward = 0L;

    @Column(name = "material_reward", nullable = false)
    @Builder.Default
    private long materialReward = 0L;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
