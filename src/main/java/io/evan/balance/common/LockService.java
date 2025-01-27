package io.evan.balance.common;

public interface LockService {
    boolean tryLock(String key);
    boolean unlock(String key);
}
