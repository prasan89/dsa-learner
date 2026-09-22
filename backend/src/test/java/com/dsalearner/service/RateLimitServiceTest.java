package com.dsalearner.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {
 @Mock StringRedisTemplate redis; @Mock ValueOperations<String,String> ops;
 @Test void allowsFirstFiveAndRejectsSixth(){
   when(redis.opsForValue()).thenReturn(ops); UUID id=UUID.randomUUID();
   when(ops.increment(anyString())).thenReturn(1L,2L,3L,4L,5L,6L);
   RateLimitService s=new RateLimitService(redis);
   for(int i=0;i<5;i++) assertTrue(s.allowAiReview(id));
   assertFalse(s.allowAiReview(id)); verify(redis).expire(anyString(),any());
 }
 @Test void remainingHandlesMissingAndUsed(){
   when(redis.opsForValue()).thenReturn(ops); UUID id=UUID.randomUUID(); RateLimitService s=new RateLimitService(redis);
   when(ops.get(anyString())).thenReturn(null); assertEquals(5,s.remainingAiReviews(id));
   when(ops.get(anyString())).thenReturn("3"); assertEquals(2,s.remainingAiReviews(id));
   when(ops.get(anyString())).thenReturn("9"); assertEquals(0,s.remainingAiReviews(id));
 }
}
