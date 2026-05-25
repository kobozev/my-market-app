package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
    Flux<CartItem> findAllByCartId(Long cartId);

    Mono<CartItem> findByCartIdAndItemId(Long cartId, Long itemId);
}