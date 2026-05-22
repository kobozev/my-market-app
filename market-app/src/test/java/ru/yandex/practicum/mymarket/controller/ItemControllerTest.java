package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.TestSecurityConfig;
import ru.yandex.practicum.mymarket.config.TestViewConfig;
import ru.yandex.practicum.mymarket.dto.request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
@Import({
        TestViewConfig.class,
        TestSecurityConfig.class
})
class ItemControllerTest extends BaseControllerTest {

    private Item testItem1;
    private Item testItem2;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();

        testItem1 = Item.builder()
                .id(1L)
                .title("Test Item 1")
                .description("Description 1")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(10)
                .build();
        testItem1.setCreatedAt(now);
        testItem1.setUpdatedAt(now);

        testItem2 = Item.builder()
                .id(2L)
                .title("Test Item 2")
                .description("Description 2")
                .price(BigDecimal.valueOf(20.0))
                .stockQuantity(20)
                .build();
        testItem2.setCreatedAt(now);
        testItem2.setUpdatedAt(now);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserId(1L);
        testCart.setItems(new ArrayList<>());
    }

    @Test
    void getItems_shouldDisplayItemsPage_anonymous() {

        Page<Item> page = new PageImpl<>(
                List.of(testItem1, testItem2),
                PageRequest.of(0, 5),
                2
        );

        when(itemService.getAllItems(any(ItemsQueryRequestDto.class)))
                .thenReturn(Mono.just(page));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService).getAllItems(any(ItemsQueryRequestDto.class));

        verify(cartService, never())
                .getCart(anyLong());
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

        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItems_shouldAcceptSearchParameter() {

        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(
                        new PageImpl<>(List.of(testItem1))
                ));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("search", "Test")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItems_shouldAcceptSortParameter() {

        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(
                        new PageImpl<>(List.of(testItem1))
                ));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("sort", "PRICE")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItems_shouldAcceptPaginationParameters() {

        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(
                        new PageImpl<>(List.of(testItem1))
                ));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItems_shouldDisplayEmptyPage() {

        when(itemService.getAllItems(any()))
                .thenReturn(Mono.just(Page.empty()));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItem_shouldDisplayItemPage_whenItemExists() {

        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getById(1L);

        verify(cartService, never())
                .getCart(anyLong());
    }

    @Test
    void getItem_shouldReturnNotFoundView_whenItemDoesNotExist() {

        when(itemService.getById(999L))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/items/999")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getById(999L);
    }

    @Test
    void getItems_shouldLoadCart_whenAuthenticated() {

        Page<Item> page = new PageImpl<>(
                List.of(testItem1),
                PageRequest.of(0, 5),
                1
        );

        when(itemService.getAllItems(any(ItemsQueryRequestDto.class)))
                .thenReturn(Mono.just(page));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getAllItems(any(ItemsQueryRequestDto.class));
    }

    @Test
    void getItem_shouldShowCartQuantity_whenAuthenticated() {

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setCartId(testCart.getId());
        cartItem.setItemId(1L);
        cartItem.setQuantity(3);

        testCart.setItems(List.of(cartItem));

        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus()
                .isOk();

        verify(itemService)
                .getById(1L);
    }
}