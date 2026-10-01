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

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 10)
    private String languageCode;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String name = "My Civilization";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CivilizationTier civilizationTier = CivilizationTier.VILLAGE;

    @Column(nullable = false)
    @Builder.Default
    private int tierLevel = 1;

    @Column(nullable = false)
    @Builder.Default
    private int totalLessonsCompleted = 0;

    @Column(nullable = false)
    @Builder.Default
    private long totalXp = 0L;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;
}
