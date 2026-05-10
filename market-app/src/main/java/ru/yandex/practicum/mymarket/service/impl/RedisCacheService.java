package ru.yandex.practicum.mymarket.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.CacheService;

import java.time.Duration;

@Service
public class RedisCacheService implements CacheService {
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final Duration cacheTtl;

    public RedisCacheService(ReactiveRedisTemplate<String, Object> redisTemplate,
                             @Value("${cache.ttl:PT1M}") Duration cacheTtl) {
        this.redisTemplate = redisTemplate;
        this.cacheTtl = cacheTtl;
    }

    @Override
    public <T> Mono<T> get(String key, Class<T> type) {
        return redisTemplate.opsForValue().get(key)
                .filter(type::isInstance)
                .cast(type);
    }

    @Override
    public Mono<Boolean> set(String key, Object value) {
        return redisTemplate.opsForValue().set(key, value, cacheTtl);
    }

    @Override
    public Mono<Long> delete(String key) {
        return redisTemplate.delete(key);
    }
}
