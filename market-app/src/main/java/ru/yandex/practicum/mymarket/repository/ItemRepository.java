package ru.yandex.practicum.mymarket.repository;

import ru.yandex.practicum.mymarket.model.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends R2dbcRepository<Item, Long> {
    Flux<Item> findAllBy(Pageable pageable);

    Flux<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}