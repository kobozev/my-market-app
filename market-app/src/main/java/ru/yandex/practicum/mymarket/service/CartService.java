package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CartService {
    Mono<Cart> getCart(Long userId);

    Mono<Void> removeItem(Long userId, Long itemId);

    Mono<Void> updateItemCount(Long userId, Long itemId, CartAction action);

    Flux<CartItem> getCartItems(Long userId);

    Mono<BigDecimal> getCartTotal(Long userId);

    Mono<Void> clear(Long userId);
}