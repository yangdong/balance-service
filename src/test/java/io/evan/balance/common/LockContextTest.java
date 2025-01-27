package io.evan.balance.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LockContextTest {

    @BeforeEach
    void clearContext() {
        RedisLockService.LockContext.removeContext("testKey");
    }

    @Test
    void testSetAndGetContextValue() {
        String key = "testKey";
        String value = "testValue";

        RedisLockService.LockContext.setContext(key, value);
        String retrievedValue = RedisLockService.LockContext.getContextValue(key);

        assertEquals(value, retrievedValue);
    }

    @Test
    void testRemoveContext() {
        String key = "testKey";
        String value = "testValue";

        RedisLockService.LockContext.setContext(key, value);
        RedisLockService.LockContext.removeContext(key);
        String retrievedValue = RedisLockService.LockContext.getContextValue(key);

        assertNull(retrievedValue);
    }

    @Test
    void testRemoveContextWhenEmpty() {
        String key = "testKey";

        RedisLockService.LockContext.removeContext(key);
        assertNull(RedisLockService.LockContext.getContextValue(key));
    }
}