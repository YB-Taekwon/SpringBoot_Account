package com.ian.account.service;

import com.ian.account.exception.AccountException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.ian.account.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber) {
        RLock lock = redissonClient.getLock(getLockKey(accountNumber));
        log.debug("Trying lock for accountNumber: {}", accountNumber);

        try {
            boolean isLock = lock.tryLock(1, 15, TimeUnit.SECONDS);
            if (!isLock) {
                log.error("Failed to lock for accountNumber: {}", accountNumber);
                throw new AccountException(ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis lock error", e);
        }
    }


    public void unlock(String accountNumber) {
        log.debug("Unlock for accountNumber: {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }


    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}
