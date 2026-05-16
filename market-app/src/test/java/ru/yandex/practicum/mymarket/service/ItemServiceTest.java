package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.constants.SortType;
import ru.yandex.practicum.mymarket.dto.Request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.dto.cache.CachedItem;
import ru.yandex.practicum.mymarket.dto.cache.CachedItemsPage;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.service.impl.ItemServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CacheService cacheService;

    private ItemServiceImpl itemService;

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, cacheService);

        item1 = Item.builder()
                .id(1L)
                .title("Laptop")
                .price(BigDecimal.valueOf(1000))
                .build();

        item2 = Item.builder()
                .id(2L)
                .title("Mouse")
                .price(BigDecimal.valueOf(50))
                .build();
    }

    @Test
    void getById_shouldReturnFromCache_whenCacheHit() {
        when(cacheService.get("item:1", Item.class))
                .thenReturn(Mono.just(item1));

        StepVerifier.create(itemService.getById(1L))
                .assertNext(item -> {
                    assertEquals(1L, item.getId());
                    assertEquals("Laptop", item.getTitle());
                })
                .verifyComplete();

        verify(itemRepository, never()).findById(anyLong());
    }

    @Test
    void getById_shouldFetchFromDbAndCache_whenCacheMiss() {
        when(cacheService.get("item:1", Item.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findById(1L))
                .thenReturn(Mono.just(item1));

        when(cacheService.set("item:1", item1))
                .thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getById(1L))
                .assertNext(item -> {
                    assertEquals(1L, item.getId());
                    assertEquals("Laptop", item.getTitle());
                })
                .verifyComplete();

        verify(itemRepository).findById(1L);
        verify(cacheService).set("item:1", item1);
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(cacheService.get("item:1", Item.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findById(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(itemService.getById(1L))
                .expectError(ItemNotFoundException.class)
                .verify();

        verify(cacheService, never()).set(anyString(), any());
    }

    @Test
    void getAllItems_shouldReturnFromCache_whenPageCacheHit() {
        ItemsQueryRequestDto request =
                new ItemsQueryRequestDto("", SortType.NO, 1, 5);

        String listKey = "list:search::sort:NO:page:1:size:5";

        CachedItemsPage cachedPage = new CachedItemsPage(
                List.of(
                        CachedItem.fromItem(item1),
                        CachedItem.fromItem(item2)
                ),
                2
        );

        when(cacheService.get(listKey, CachedItemsPage.class))
                .thenReturn(Mono.just(cachedPage));

        when(cacheService.get("item:1", Item.class))
                .thenReturn(Mono.just(item1));

        when(cacheService.get("item:2", Item.class))
                .thenReturn(Mono.just(item2));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(2, page.getTotalElements());
                })
                .verifyComplete();

        verify(itemRepository, never()).findAllBy(any(Pageable.class));
    }

    @Test
    void getAllItems_shouldFetchFromDb_whenCacheMiss() {
        ItemsQueryRequestDto request =
                new ItemsQueryRequestDto("", SortType.NO, 1, 10);

        String listKey = "list:search::sort:NO:page:1:size:10";

        when(cacheService.get(listKey, CachedItemsPage.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findAllBy(any(Pageable.class)))
                .thenReturn(Flux.just(item1, item2));

        when(itemRepository.count())
                .thenReturn(Mono.just(2L));

        when(cacheService.set(eq("item:1"), eq(item1)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq("item:2"), eq(item2)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq(listKey), any(CachedItemsPage.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(2, page.getTotalElements());
                })
                .verifyComplete();

        verify(itemRepository).findAllBy(any(Pageable.class));
        verify(cacheService).set(eq(listKey), any(CachedItemsPage.class));
    }

    @Test
    void getAllItems_shouldFilterBySearch() {
        ItemsQueryRequestDto request =
                new ItemsQueryRequestDto("lap", SortType.NO, 1, 10);

        String listKey = "list:search:lap:sort:NO:page:1:size:10";

        when(cacheService.get(listKey, CachedItemsPage.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findByTitleContainingIgnoreCase(eq("lap"), any(Pageable.class)))
                .thenReturn(Flux.just(item1));

        when(itemRepository.count())
                .thenReturn(Mono.just(1L));

        when(cacheService.set(eq("item:1"), eq(item1)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq(listKey), any(CachedItemsPage.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals("Laptop", page.getContent().getFirst().getTitle());
                })
                .verifyComplete();

        verify(itemRepository)
                .findByTitleContainingIgnoreCase(eq("lap"), any(Pageable.class));
    }

    @Test
    void getAllItems_shouldSortByPrice() {
        ItemsQueryRequestDto request =
                new ItemsQueryRequestDto("", SortType.PRICE, 1, 10);

        String listKey = "list:search::sort:PRICE:page:1:size:10";

        when(cacheService.get(listKey, CachedItemsPage.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findAllBy(any(Pageable.class)))
                .thenReturn(Flux.just(item2, item1));

        when(itemRepository.count())
                .thenReturn(Mono.just(2L));

        when(cacheService.set(eq("item:1"), eq(item1)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq("item:2"), eq(item2)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq(listKey), any(CachedItemsPage.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> assertEquals(2, page.getContent().size()))
                .verifyComplete();

        verify(itemRepository).findAllBy(argThat(pageable ->
                pageable.getSort().getOrderFor("price") != null
        ));
    }

    @Test
    void getAllItems_shouldUseCorrectPageOffset() {
        ItemsQueryRequestDto request =
                new ItemsQueryRequestDto("", SortType.NO, 3, 10);

        String listKey = "list:search::sort:NO:page:3:size:10";

        when(cacheService.get(listKey, CachedItemsPage.class))
                .thenReturn(Mono.empty());

        when(itemRepository.findAllBy(any(Pageable.class)))
                .thenReturn(Flux.just(item1));

        when(itemRepository.count())
                .thenReturn(Mono.just(25L));

        when(cacheService.set(eq("item:1"), eq(item1)))
                .thenReturn(Mono.just(true));

        when(cacheService.set(eq(listKey), any(CachedItemsPage.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(2, page.getNumber());
                    assertEquals(10, page.getSize());
                })
                .verifyComplete();

        verify(itemRepository).findAllBy(argThat(pageable ->
                pageable.getPageNumber() == 2 &&
                        pageable.getPageSize() == 10
        ));
    }
}