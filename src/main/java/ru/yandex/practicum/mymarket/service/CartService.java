package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.dto.CartDto;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface CartService {
    Mono<CartDto> getCart(WebSession session);

    Mono<Void> removeItem(WebSession session, long itemId);

    Mono<Void> updateItemCount(WebSession session, long itemId, CartAction action);

    Flux<CartItemDto> getCartItems(WebSession session);

    Mono<BigDecimal> getCartTotal(WebSession session);

    Mono<Void> clear(WebSession session);
}