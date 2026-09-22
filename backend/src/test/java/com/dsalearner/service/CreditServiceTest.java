package com.dsalearner.service;

import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {
 @Mock AiCreditWalletRepository walletRepo; @Mock CreditTransactionRepository txRepo; @Mock UserRepository userRepo;
 private CreditService service(){return new CreditService(walletRepo,txRepo,userRepo);}
 @Test void createsWelcomeWallet(){
   UUID id=UUID.randomUUID(); User u=mock(User.class); when(userRepo.getReferenceById(id)).thenReturn(u);
   when(walletRepo.findByUserId(id)).thenReturn(Optional.empty());
   AiCreditWallet w=mock(AiCreditWallet.class); when(walletRepo.save(any(AiCreditWallet.class))).thenReturn(w);
   AiCreditWallet result=service().getOrCreateWallet(id); assertSame(w,result); verify(txRepo).save(any(CreditTransaction.class));
 }
 @Test void deductsFreeCredits(){UUID id=UUID.randomUUID();AiCreditWallet w=mock(AiCreditWallet.class);when(w.totalCredits()).thenReturn(10);when(w.getFreeCredits()).thenReturn(10);when(walletRepo.findByUserId(id)).thenReturn(Optional.of(w));assertTrue(service().deductForReview(id));verify(w).setFreeCredits(9);}
 @Test void refusesWhenEmpty(){UUID id=UUID.randomUUID();AiCreditWallet w=mock(AiCreditWallet.class);when(w.totalCredits()).thenReturn(0);when(walletRepo.findByUserId(id)).thenReturn(Optional.of(w));assertFalse(service().deductForDetect(id));}
 @Test void addsPaidCredits(){UUID id=UUID.randomUUID();AiCreditWallet w=mock(AiCreditWallet.class);User u=mock(User.class);when(w.getPaidCredits()).thenReturn(5);when(w.getUser()).thenReturn(u);when(walletRepo.findByUserId(id)).thenReturn(Optional.of(w));service().addPaidCredits(id,50,"purchase");verify(w).setPaidCredits(55);verify(txRepo).save(any());}
}
