package com.example.first_project.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
@Service
public class RateLimitingService {
    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitingService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    public boolean isAllowed(String key, int maxRequests, int timeWindowMinutes) {
        String redisKey = "rate_limit:" + key;
        Long currentCount = stringRedisTemplate.opsForValue().increment(redisKey);
        if (currentCount == 1) {
            stringRedisTemplate.expire(redisKey, Duration.ofMinutes(timeWindowMinutes));
        }
        return currentCount <= maxRequests;
    }
}
