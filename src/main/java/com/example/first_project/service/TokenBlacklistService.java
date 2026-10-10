package com.example.first_project.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token, long expirationMilliseconds) {
        redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofMillis(expirationMilliseconds));
    }

    public Long getExpirationTime(String token) {
        return redisTemplate.getExpire(token);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}
