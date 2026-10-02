package com.dsalearner.civilization.model.entity;

import com.dsalearner.civilization.domain.CivilizationTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_civilizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LangoaCivilization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String name = "My Civilization";

    @Enumerated(EnumType.STRING)
    @Column(name = "civilization_tier", nullable = false, length = 20)
    @Builder.Default
    private CivilizationTier civilizationTier = CivilizationTier.VILLAGE;

    @Column(name = "tier_level", nullable = false)
    @Builder.Default
    private int tierLevel = 1;

    @Column(name = "total_lessons_completed", nullable = false)
    @Builder.Default
    private int totalLessonsCompleted = 0;

    @Column(name = "total_xp", nullable = false)
    @Builder.Default
    private long totalXp = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
