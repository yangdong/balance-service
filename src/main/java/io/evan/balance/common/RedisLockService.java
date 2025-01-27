package io.evan.balance.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisLockService implements LockService {
    private final StringRedisTemplate redisTemplate;
    private final String LOCK_PREFIX = "LOCK:";
    private final long DEFAULT_LOCK_TIMEOUT = 30000; // 30 seconds
    private final long DEFAULT_WAIT_TIMEOUT = 3000;  // 3 seconds

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_LOCK_TIMEOUT, DEFAULT_WAIT_TIMEOUT);
    }

    public boolean tryLock(String key, long timeoutMillis, long waitTimeMillis) {
        final String lockKey = LOCK_PREFIX + key;
        final String randomValue = UUID.randomUUID().toString();
        final long startTime = System.currentTimeMillis();

        try {
            while ((System.currentTimeMillis() - startTime) < waitTimeMillis) {
                Boolean success = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, randomValue, timeoutMillis, TimeUnit.MILLISECONDS);

                if (Boolean.TRUE.equals(success)) {
                    LockContext.setContext(lockKey, randomValue);
                    return true;
                }

                Thread.sleep(100); // 短暂休眠后重试
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error acquiring lock for key: {}", key, e);
        }

        return false;
    }

    @Override
    public boolean unlock(final String key) {
        final String lockKey = LOCK_PREFIX + key;
        final String expectedValue = LockContext.getContextValue(lockKey);

        if (expectedValue == null) {
            log.warn("No lock context found for key: {}", key);
            return false;
        }

        try {
            // 使用Lua脚本保证原子性
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

            Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList(lockKey),
                    expectedValue);

            return Long.valueOf(1L).equals(result);
        } finally {
            LockContext.removeContext(lockKey);
        }
    }

    public static class LockContext {
        private static final ThreadLocal<Map<String, String>> CONTEXT = ThreadLocal.withInitial(HashMap::new);

        public static void setContext(String key, String value) {
            CONTEXT.get().put(key, value);
        }

        public static String getContextValue(String key) {
            return CONTEXT.get().get(key);
        }

        public static void removeContext(String key) {
            CONTEXT.get().remove(key);
            if (CONTEXT.get().isEmpty()) {
                CONTEXT.remove();
            }
        }
    }

}

