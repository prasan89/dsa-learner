package com.dsalearner.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterTest {
 @Mock StringRedisTemplate redis; @Mock ValueOperations<String,String> ops;
 @Test void allowsFirstFiveAndBlocksSixth(){when(redis.opsForValue()).thenReturn(ops);when(ops.increment(anyString())).thenReturn(1L,2L,3L,4L,5L,6L);LoginRateLimiter s=new LoginRateLimiter(redis);for(int i=0;i<5;i++)assertTrue(s.isAllowed("ip"));assertFalse(s.isAllowed("ip"));verify(redis).expire(anyString(),any());}
 @Test void nullRedisCountIsAllowed(){when(redis.opsForValue()).thenReturn(ops);when(ops.increment(anyString())).thenReturn(null);assertTrue(new LoginRateLimiter(redis).isAllowed("ip"));}
 @Test void resetDeletesKey(){new LoginRateLimiter(redis).resetAttempts("ip");verify(redis).delete("login_attempts:ip");}
}
