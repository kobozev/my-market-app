package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Flux<Order> getAll(Long userId);

    Mono<Order> getById(long id, Long userId);

    Mono<Order> create(List<CartItemDto> cartItems, Long userId);
}
