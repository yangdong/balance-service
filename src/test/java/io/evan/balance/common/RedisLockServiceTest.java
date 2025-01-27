package io.evan.balance.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisLockServiceTest {

    @Mock
    private StringRedisTemplate mockRedisTemplate;

    private RedisLockService redisLockService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisLockService = new RedisLockService(mockRedisTemplate);
    }

    @Test
    void testUnlockSuccess() {
        String key = "testKey";
        RedisLockService.LockContext.setContext("LOCK:" + key, "randomValue");
        when(mockRedisTemplate.execute(any(), anyList(), anyString()))
                .thenReturn(1L);

        boolean result = redisLockService.unlock(key);

        assertTrue(result);
    }

    @Test
    void testUnlockFailure() {
        String key = "testKey";
        RedisLockService.LockContext.setContext("LOCK:" + key, "randomValue");
        when(mockRedisTemplate.execute(any(), anyList(), anyString()))
                .thenReturn(0L);

        boolean result = redisLockService.unlock(key);

        assertFalse(result);
    }
}