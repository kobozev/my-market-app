package ru.yandex.practicum.mymarket.service.impl;

import ru.yandex.practicum.mymarket.constants.SortType;
import ru.yandex.practicum.mymarket.dto.Request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.Set;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Mono<Page<Item>> getAllItems(ItemsQueryRequestDto queryRequest) {
        Pageable pageable = createPageable(queryRequest);

        var search = queryRequest.getSearch();
        if (search.isEmpty()) {
            return getItemsPageableStream(itemRepository.findAllBy(pageable), pageable);
        }
        return getItemsPageableStream(itemRepository.findByTitleContainingIgnoreCase(search, pageable), pageable);
    }

    @Override
    public Mono<Item> getById(Long id) {
        return itemRepository.findById(id);
    }

    @Override
    public Flux<Item> getByIds(Set<Long> ids) {
        return itemRepository.findAllById(ids);
    }

    private Pageable createPageable(ItemsQueryRequestDto queryRequest) {
        Sort sort = switch (queryRequest.getSort()) {
            case SortType.ALPHA -> Sort.by("title").ascending();
            case SortType.PRICE -> Sort.by("price").ascending();
            default -> Sort.unsorted();
        };

        return PageRequest.of(queryRequest.getPageNumber() - 1, queryRequest.getPageSize(), sort);
    }

    private Mono<Page<Item>> getItemsPageableStream(Flux<Item> itemsStream, Pageable pageable) {
        return itemsStream
                .collectList()
                .zipWith(itemRepository.count())
                .map(objects ->
                        new PageImpl<>(objects.getT1(), pageable, objects.getT2()));
    }
}