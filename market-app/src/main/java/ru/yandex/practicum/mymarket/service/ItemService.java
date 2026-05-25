package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.model.Item;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

public interface ItemService {
    Mono<Page<Item>> getAllItems(ItemsQueryRequestDto queryRequest);

    Mono<Item> getById(Long id);

    Flux<Item> getByIds(Set<Long> ids);
}