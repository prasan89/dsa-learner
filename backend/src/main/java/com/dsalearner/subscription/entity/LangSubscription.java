package com.dsalearner.subscription.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lang_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangSubscription {

    public enum Status { ACTIVE, CANCELLED, EXPIRED, PENDING, PAST_DUE }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String planCode = "FREE";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.ACTIVE;

    @Column(length = 500)
    private String playPurchaseToken;

    @Column(length = 100)
    private String playOrderId;

    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private Instant cancelledAt;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @UpdateTimestamp
    private Instant updatedAt;

    public boolean isProActive() {
        return status == Status.ACTIVE
                && !"FREE".equals(planCode)
                && (currentPeriodEnd == null || currentPeriodEnd.isAfter(Instant.now()));
    }
}
