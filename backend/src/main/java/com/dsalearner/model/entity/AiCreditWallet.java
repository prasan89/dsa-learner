package com.dsalearner.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_credit_wallets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AiCreditWallet {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "free_credits", nullable = false)
    private int freeCredits = 10;

    @Column(name = "paid_credits", nullable = false)
    private int paidCredits = 0;

    @Column(name = "lifetime_used", nullable = false)
    private int lifetimeUsed = 0;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public int totalCredits() {
        return freeCredits + paidCredits;
    }
}
