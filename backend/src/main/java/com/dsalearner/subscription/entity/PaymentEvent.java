package com.dsalearner.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lang_payment_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {

    public enum EventStatus { RECEIVED, PROCESSED, FAILED, DUPLICATE }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @Column(length = 500)
    private String playPurchaseToken;

    @Column(length = 100)
    private String playOrderId;

    @Column(nullable = false, length = 60)
    private String eventType;

    @Column(length = 30)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EventStatus status = EventStatus.RECEIVED;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

    private Instant processedAt;
    private String failureReason;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
