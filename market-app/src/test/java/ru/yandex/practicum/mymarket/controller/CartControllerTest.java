package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
@Import(TestViewConfig.class)
class CartControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected CartService cartService;

    private CartItem testCartItem;

    @BeforeEach
    void setUp() {

        Item testItem = Item.builder()
                .id(1L)
                .title("Test Item")
                .price(BigDecimal.valueOf(10.0))
                .stockQuantity(10)
                .build();

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCartId(1L);
        testCartItem.setItemId(1L);
        testCartItem.setQuantity(2);
        testCartItem.setItem(testItem);
    }

    @Test
    void getCartItems_shouldReturnCartPage() {

        when(cartService.getCartItems(anyString()))
                .thenReturn(Flux.just(testCartItem));

        when(cartService.getCartTotal(anyString()))
                .thenReturn(Mono.just(BigDecimal.valueOf(20)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus()
                .isOk();

        verify(cartService).getCartItems(anyString());
        verify(cartService).getCartTotal(anyString());
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsPlus() {

        when(cartService.updateItemCount(anyString(), eq(1L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "5")
                        .build()
                )
                .bodyValue(Map.of("action", "PLUS"))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items?search=&sort=NO&pageNumber=1&pageSize=5");

        verify(cartService)
                .updateItemCount(anyString(), eq(1L), eq(CartAction.PLUS));
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsMinus() {

        when(cartService.updateItemCount(anyString(), eq(1L), eq(CartAction.MINUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "MINUS")
                        .queryParam("search", "test")
                        .queryParam("sort", "PRICE")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "10")
                        .build()
                )
                .bodyValue(Map.of("action", "MINUS"))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items?search=test&sort=PRICE&pageNumber=2&pageSize=10");

        verify(cartService)
                .updateItemCount(anyString(), eq(1L), eq(CartAction.MINUS));
    }

    @Test
    void addOrRemoveToCart_shouldRedirectToItemDetail_whenIdInPath() {

        when(cartService.updateItemCount(anyString(), eq(5L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/5")
                        .queryParam("id", "5")
                        .queryParam("action", "PLUS")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "5")
                        .build()
                )
                .bodyValue(Map.of("action", "PLUS"))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items/5");

        verify(cartService)
                .updateItemCount(anyString(), eq(5L), eq(CartAction.PLUS));
    }

    @Test
    void updateCartFromCartPage_shouldUpdateAndReturnCartPage() {

        when(cartService.updateItemCount(anyString(), eq(1L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        when(cartService.getCartItems(anyString()))
                .thenReturn(Flux.just(testCartItem));

        when(cartService.getCartTotal(anyString()))
                .thenReturn(Mono.just(BigDecimal.valueOf(30.0)));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build()
                )
                .bodyValue(Map.of("action", "PLUS"))
                .exchange()
                .expectStatus()
                .isOk();

        verify(cartService)
                .updateItemCount(anyString(), eq(1L), eq(CartAction.PLUS));

        verify(cartService)
                .getCartItems(anyString());

        verify(cartService)
                .getCartTotal(anyString());
    }

    @Test
    void addOrRemoveToCart_shouldNotUpdateCart_whenNoAction() {

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("search", "")
                        .queryParam("sort", "NO")
                        .queryParam("pageNumber", "1")
                        .queryParam("pageSize", "5")
                        .build()
                )
                .exchange()
                .expectStatus()
                .is4xxClientError();

        verify(cartService, never())
                .updateItemCount(anyString(), anyLong(), any());
    }
}