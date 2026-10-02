package com.dsalearner.subscription.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "lang_subscription_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlan {

    @Id
    @Column(name = "plan_code", length = 30)
    private String planCode;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private int pricePaise = 0;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    @Column(length = 100)
    private String playProductId;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
