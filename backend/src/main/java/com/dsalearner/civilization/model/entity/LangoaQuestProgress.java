package com.dsalearner.civilization.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "langoa_quest_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaQuestProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "quest_id", nullable = false)
    private UUID questId;

    @Column(name = "current_count", nullable = false)
    @Builder.Default
    private int currentCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "reward_claimed", nullable = false)
    @Builder.Default
    private boolean rewardClaimed = false;

    @Column(name = "quest_date", nullable = false)
    @Builder.Default
    private LocalDate questDate = LocalDate.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
