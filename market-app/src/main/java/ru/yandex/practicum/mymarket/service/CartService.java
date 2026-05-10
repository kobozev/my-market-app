package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CartService {
    Mono<Cart> getCart(String sessionId);

    Mono<Void> removeItem(String sessionId, Long itemId);

    Mono<Void> updateItemCount(String sessionId, Long itemId, CartAction action);

    Flux<CartItem> getCartItems(String sessionId);

    Mono<BigDecimal> getCartTotal(String sessionId);

    Mono<Void> clear(String sessionId);
}