package com.dsalearner.economy.antiabuse;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "langoa_suspicious_activity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangoaSuspiciousActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @Column(nullable = false, length = 80)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
