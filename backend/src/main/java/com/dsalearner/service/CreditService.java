package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditService {

    private static final int FREE_REVIEW_COST  = 1;
    private static final int FREE_HINT_COST    = 1;
    private static final int FREE_DETECT_COST  = 1;
    private static final int NEW_USER_CREDITS  = 10;

    private final AiCreditWalletRepository walletRepository;
    private final CreditTransactionRepository txRepository;
    private final UserRepository userRepository;

    @Transactional
    public AiCreditWallet getOrCreateWallet(UUID userId) {
        return walletRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.getReferenceById(userId);
            AiCreditWallet wallet = AiCreditWallet.builder()
                    .user(user)
                    .freeCredits(NEW_USER_CREDITS)
                    .paidCredits(0)
                    .lifetimeUsed(0)
                    .updatedAt(Instant.now())
                    .build();
            wallet = walletRepository.save(wallet);
            recordTransaction(user, NEW_USER_CREDITS, CreditTransaction.TxType.FREE_GRANT, "Welcome credits");
            return wallet;
        });
    }

    @Transactional
    public boolean deductForReview(UUID userId) {
        return deduct(userId, FREE_REVIEW_COST, CreditTransaction.TxType.AI_REVIEW, "AI code review");
    }

    @Transactional
    public boolean deductForHint(UUID userId) {
        return deduct(userId, FREE_HINT_COST, CreditTransaction.TxType.AI_HINT, "AI hint unlock");
    }

    @Transactional
    public boolean deductForDetect(UUID userId) {
        return deduct(userId, FREE_DETECT_COST, CreditTransaction.TxType.AI_DETECT, "Pattern detection");
    }

    @Transactional
    public void addPaidCredits(UUID userId, int amount, String desc) {
        AiCreditWallet wallet = getOrCreateWallet(userId);
        wallet.setPaidCredits(wallet.getPaidCredits() + amount);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);
        recordTransaction(wallet.getUser(), amount, CreditTransaction.TxType.PURCHASE, desc);
    }

    private boolean deduct(UUID userId, int cost, CreditTransaction.TxType type, String desc) {
        AiCreditWallet wallet = getOrCreateWallet(userId);
        if (wallet.totalCredits() < cost) return false;

        // Deduct free first, then paid
        int remaining = cost;
        if (wallet.getFreeCredits() >= remaining) {
            wallet.setFreeCredits(wallet.getFreeCredits() - remaining);
        } else {
            remaining -= wallet.getFreeCredits();
            wallet.setFreeCredits(0);
            wallet.setPaidCredits(wallet.getPaidCredits() - remaining);
        }
        wallet.setLifetimeUsed(wallet.getLifetimeUsed() + cost);
        wallet.setUpdatedAt(Instant.now());
        walletRepository.save(wallet);

        recordTransaction(wallet.getUser(), -cost, type, desc);
        return true;
    }

    private void recordTransaction(User user, int delta, CreditTransaction.TxType type, String desc) {
        txRepository.save(CreditTransaction.builder()
                .user(user)
                .delta(delta)
                .type(type)
                .description(desc)
                .createdAt(Instant.now())
                .build());
    }

    public record WalletResponse(int freeCredits, int paidCredits, int totalCredits,
                                  int lifetimeUsed, List<TxResponse> recentTransactions) {}
    public record TxResponse(int delta, String type, String description, Instant createdAt) {}

    public WalletResponse getWallet(UUID userId) {
        AiCreditWallet wallet = getOrCreateWallet(userId);
        List<TxResponse> txs = txRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 20))
                .stream()
                .map(t -> new TxResponse(t.getDelta(), t.getType().name(), t.getDescription(), t.getCreatedAt()))
                .toList();
        return new WalletResponse(wallet.getFreeCredits(), wallet.getPaidCredits(),
                wallet.totalCredits(), wallet.getLifetimeUsed(), txs);
    }
}
