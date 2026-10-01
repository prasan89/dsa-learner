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

    @Column(nullable = false, unique = true, length = 100)
    private String questKey;

    @Column(nullable = false, length = 200)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String questType = "DAILY";

    @Column(nullable = false, length = 50)
    private String targetType;

    @Column(nullable = false)
    @Builder.Default
    private int targetCount = 1;

    @Column(nullable = false)
    @Builder.Default
    private long xpReward = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long coinReward = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long foodReward = 0L;

    @Column(nullable = false)
    @Builder.Default
    private long materialReward = 0L;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
