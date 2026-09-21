package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "credit_transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreditTransaction {

    public enum TxType { FREE_GRANT, PURCHASE, AI_REVIEW, AI_HINT, AI_DETECT }

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int delta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TxType type;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
