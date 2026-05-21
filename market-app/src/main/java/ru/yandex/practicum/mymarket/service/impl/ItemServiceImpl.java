package ru.yandex.practicum.mymarket.service.impl;

import ru.yandex.practicum.mymarket.constants.SortType;
import ru.yandex.practicum.mymarket.dto.request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.dto.cache.CachedItem;
import ru.yandex.practicum.mymarket.dto.cache.CachedItemsPage;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.CacheService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.List;
import java.util.Set;

@Service
public class ItemServiceImpl implements ItemService {
    private static final String ITEM_CACHE_KEY_PREFIX = "item:";
    private static final String LIST_CACHE_KEY_PREFIX = "list:";

    private final ItemRepository itemRepository;
    private final CacheService cacheService;

    public ItemServiceImpl(ItemRepository itemRepository, CacheService cacheService) {
        this.itemRepository = itemRepository;
        this.cacheService = cacheService;
    }

    @Override
    public Mono<Page<Item>> getAllItems(ItemsQueryRequestDto queryRequest) {
        String cacheKey = buildListCacheKey(queryRequest);
        Pageable pageable = createPageable(queryRequest);

        return cacheService.get(cacheKey, CachedItemsPage.class)
                .flatMap(cachedPage -> fetchFullItemsFromCache(cachedPage, pageable))
                .switchIfEmpty(Mono.defer(() -> fetchFromDatabase(queryRequest, pageable, cacheKey)));
    }

    @Override
    public Mono<Item> getById(Long id) {
        String key = ITEM_CACHE_KEY_PREFIX + id;
        return cacheService.get(key, Item.class)
                .switchIfEmpty(Mono.defer(() ->
                        itemRepository.findById(id)
                                .switchIfEmpty(Mono.error(new ItemNotFoundException(
                                        String.format("Item with id %d not found", id))))
                                .flatMap(item -> cacheService.set(key, item)
                                        .thenReturn(item))
                ));
    }

    @Override
    public Flux<Item> getByIds(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(ids)
                .flatMapSequential(this::getById);
    }

    private Pageable createPageable(ItemsQueryRequestDto queryRequest) {
        Sort sort = switch (queryRequest.getSort()) {
            case SortType.ALPHA -> Sort.by("title").ascending();
            case SortType.PRICE -> Sort.by("price").ascending();
            default -> Sort.unsorted();
        };

        return PageRequest.of(queryRequest.getPageNumber() - 1, queryRequest.getPageSize(), sort);
    }

    private String buildListCacheKey(ItemsQueryRequestDto request) {
        return LIST_CACHE_KEY_PREFIX +
                "search:" + request.getSearch().toLowerCase() +
                ":sort:" + request.getSort() +
                ":page:" + request.getPageNumber() +
                ":size:" + request.getPageSize();
    }

    private Mono<Page<Item>> fetchFullItemsFromCache(CachedItemsPage cachedItemsPage, Pageable pageable) {
        List<Long> ids = cachedItemsPage.getItems().stream()
                .map(CachedItem::getId)
                .toList();

        return Flux.fromIterable(ids)
                .flatMapSequential(this::getById)
                .collectList()
                .map(items -> new PageImpl<>(items, pageable, cachedItemsPage.getTotal()));
    }

    private Mono<Page<Item>> fetchFromDatabase(ItemsQueryRequestDto request, Pageable pageable, String cacheKey) {
        Flux<Item> itemsFlux = request.getSearch().isEmpty()
                ? itemRepository.findAllBy(pageable)
                : itemRepository.findByTitleContainingIgnoreCase(request.getSearch(), pageable);

        return itemsFlux
                .flatMap(this::cacheItem)
                .collectList()
                .zipWith(itemRepository.count())
                .flatMap(tuple -> {
                    List<Item> items = tuple.getT1();
                    long total = tuple.getT2();

                    List<CachedItem> cachedItems = items.stream()
                            .map(CachedItem::fromItem)
                            .toList();
                    CachedItemsPage cachedItemsPage = new CachedItemsPage(cachedItems, total);
                    return cacheService.set(cacheKey, cachedItemsPage)
                            .thenReturn(new PageImpl<>(items, pageable, total));
                });
    }

    private Mono<Item> cacheItem(Item item) {
        String key = ITEM_CACHE_KEY_PREFIX + item.getId();
        return cacheService.set(key, item)
                .thenReturn(item);
    }
}