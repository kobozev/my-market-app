package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
