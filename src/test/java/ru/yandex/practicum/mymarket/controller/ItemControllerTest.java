package ru.yandex.practicum.mymarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.dto.Request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
@Import(TestViewConfig.class)
class ItemControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected ItemService itemService;

    private Item testItem1;
    private Item testItem2;
    private CartDto cartDto;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();

        testItem1 = Item.builder()
                .id(1L)
                .title("Test Item 1")
                .description("Description 1")
                .price(BigDecimal.valueOf(10.0))
                .build();
        testItem1.setCreatedAt(now);
        testItem1.setUpdatedAt(now);

        testItem2 = Item.builder()
                .id(2L)
                .title("Test Item 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(20.0))
                .build();
        testItem2.setCreatedAt(now);
        testItem2.setUpdatedAt(now);

        cartDto = new CartDto();
    }

    @Test
    void getItems_shouldDisplayItemsPage() {
        Page<Item> page = new PageImpl<>(
                List.of(testItem1, testItem2),
                PageRequest.of(0, 5),
                2
        );

        when(itemService.getAllItems(any(ItemsQueryRequestDto.class)))
                .thenReturn(Mono.just(page));
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any(ItemsQueryRequestDto.class));
        verify(cartService).getCart(any(WebSession.class));
    }

    @Test
    void getItems_shouldDisplayItemsPageAtRootUrl() {
        Page<Item> page = new PageImpl<>(
                List.of(testItem1),
                PageRequest.of(0, 5),
                1
        );

        when(itemService.getAllItems(any(ItemsQueryRequestDto.class)))
                .thenReturn(Mono.just(page));
        when(cartService.getCart(any(WebSession.class)))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItems_shouldAcceptSearchParameter() {
        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testItem1))));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "Test")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any());
    }

    @Test
    void getItems_shouldAcceptSortParameter() {
        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testItem1))));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("sort", "PRICE")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any());
    }

    @Test
    void getItems_shouldAcceptPaginationParameters() {
        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testItem1))));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any());
    }

    @Test
    void getItems_shouldDisplayEmptyPage() {
        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(Page.empty()));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getAllItems(any());
    }

    @Test
    void getItem_shouldDisplayItemPage_whenItemExists() {
        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getById(1L);
        verify(cartService).getCart(any());
    }

    @Test
    void getItem_shouldReturnNotFoundView_whenItemDoesNotExist() {
        when(itemService.getById(999L))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/items/999")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getById(999L);
    }

    @Test
    void getItem_shouldShowCartQuantity_whenItemInCart() {
        cartDto.addItem(1L, 3);

        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));
        when(cartService.getCart(any()))
                .thenReturn(Mono.just(cartDto));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();

        verify(itemService).getById(1L);
        verify(cartService).getCart(any());
    }
}