package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.TestSecurityConfig;
import ru.yandex.practicum.mymarket.config.TestViewConfig;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.payment.client.model.BalanceResponse;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
@Import({
        TestViewConfig.class,
        TestSecurityConfig.class
})
class CartControllerTest extends BaseControllerTest {

    private CartItem testCartItem;
    private BalanceResponse testBalance;

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

        testBalance = new BalanceResponse()
                .userId(1L)
                .balance(BigDecimal.valueOf(100.0));
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsPlus() {

        when(cartService.updateItemCount(anyLong(), eq(1L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        authenticatedClient().post()
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
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items?search=&sort=NO&pageNumber=1&pageSize=5");

        verify(cartService)
                .updateItemCount(anyLong(), eq(1L), eq(CartAction.PLUS));
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsMinus() {

        when(cartService.updateItemCount(anyLong(), eq(1L), eq(CartAction.MINUS)))
                .thenReturn(Mono.empty());

        authenticatedClient().post()
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
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items?search=test&sort=PRICE&pageNumber=2&pageSize=10");

        verify(cartService)
                .updateItemCount(anyLong(), eq(1L), eq(CartAction.MINUS));
    }

    @Test
    void addOrRemoveToCart_shouldRedirectToItemDetail_whenIdInPath() {

        when(cartService.updateItemCount(anyLong(), eq(5L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        authenticatedClient().post()
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
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/items/5");

        verify(cartService)
                .updateItemCount(anyLong(), eq(5L), eq(CartAction.PLUS));
    }

    @Test
    void addOrRemoveToCart_shouldNotUpdateCart_whenNoAction() {

        authenticatedClient().post()
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
                .updateItemCount(anyLong(), anyLong(), any());
    }

    @Test
    void getCartItems_shouldReturnUnauthorized_whenUserNotAuthenticated() {

        webTestClient.get()
                .uri("/cart/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .isUnauthorized();

        verify(cartService, never()).getCartItems(anyLong());
    }
}