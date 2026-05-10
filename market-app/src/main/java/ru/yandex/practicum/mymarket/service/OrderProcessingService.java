package ru.yandex.practicum.mymarket.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;

public interface OrderProcessingService {
    Mono<Order> checkout(String sessionId);
}