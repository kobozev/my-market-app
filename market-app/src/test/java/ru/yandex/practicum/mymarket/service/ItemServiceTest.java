package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.constants.SortType;
import ru.yandex.practicum.mymarket.dto.Request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.service.impl.ItemServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
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
    void getById_shouldReturnItem_whenExists() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));

        StepVerifier.create(itemService.getById(1L))
                .assertNext(item -> assertEquals(1L, item.getId()))
                .verifyComplete();
    }

    @Test
    void getById_shouldThrowException_whenNotExists() {
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getById(1L))
                .expectError(ItemNotFoundException.class)
                .verify();
    }

    @Test
    void getByIds_shouldReturnItems_whenAllExist() {
        when(itemRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(Flux.just(item1, item2));

        StepVerifier.create(itemService.getByIds(Set.of(1L, 2L)).collectList())
                .assertNext(items -> {
                    assertEquals(2, items.size());
                    assertTrue(items.contains(item1));
                    assertTrue(items.contains(item2));
                })
                .verifyComplete();
    }

    @Test
    void getAllItems_shouldReturnAllItems_whenSearchEmpty() {
        ItemsQueryRequestDto request = new ItemsQueryRequestDto("", SortType.NO, 1, 10);

        when(itemRepository.findAllBy(any()))
                .thenReturn(Flux.just(item1, item2));

        when(itemRepository.count())
                .thenReturn(Mono.just(2L));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(2, page.getTotalElements());
                })
                .verifyComplete();
    }

    @Test
    void getAllItems_shouldFilterBySearch() {
        ItemsQueryRequestDto request = new ItemsQueryRequestDto("lap", SortType.NO, 1, 10);

        when(itemRepository.findByTitleContainingIgnoreCase(eq("lap"), any()))
                .thenReturn(Flux.just(item1));

        when(itemRepository.count())
                .thenReturn(Mono.just(1L));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals("Laptop", page.getContent().getFirst().getTitle());
                })
                .verifyComplete();
    }

    @Test
    void getAllItems_shouldSortByPrice() {
        ItemsQueryRequestDto request = new ItemsQueryRequestDto("", SortType.PRICE, 1, 10);

        when(itemRepository.findAllBy(any()))
                .thenReturn(Flux.just(item2, item1));

        when(itemRepository.count())
                .thenReturn(Mono.just(2L));

        StepVerifier.create(itemService.getAllItems(request))
                .assertNext(page -> {
                    List<Item> items = page.getContent();
                    assertEquals(2, items.size());
                })
                .verifyComplete();
    }
}