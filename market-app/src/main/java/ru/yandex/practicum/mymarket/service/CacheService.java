package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Mono;

public interface CacheService {
    <T> Mono<T> get(String key, Class<T> type);

    Mono<Boolean> set(String key, Object value);

    Mono<Long> delete(String key);
}
