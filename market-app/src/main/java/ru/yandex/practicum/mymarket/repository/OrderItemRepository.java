package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
    Flux<OrderItem> findByOrderId(Long orderId);
}