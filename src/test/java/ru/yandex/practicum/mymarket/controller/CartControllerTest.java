package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;

@WebFluxTest(CartController.class)
@Import(TestViewConfig.class)
class CartControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @MockitoBean
    protected CartService cartService;

    @Test
    void getCartItems_shouldReturnCartPage() {
        Item item = Item.builder().id(1L).title("Item").price(BigDecimal.TEN).stockQuantity(10).build();

        when(cartService.getCartItems(any(WebSession.class)))
                .thenReturn(Flux.just(new CartItemDto(item, 2)));

        when(cartService.getCartTotal(any(WebSession.class)))
                .thenReturn(Mono.just(BigDecimal.valueOf(20)));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();

        verify(cartService).getCartItems(any(WebSession.class));
        verify(cartService).getCartTotal(any(WebSession.class));
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsPlus() {
        when(cartService.updateItemCount(any(WebSession.class), anyLong(), any()))
                .thenReturn(Mono.empty());

        when(cartService.getCartItems(any(WebSession.class)))
                .thenReturn(Flux.empty());

        when(cartService.getCartTotal(any(WebSession.class)))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).updateItemCount(any(WebSession.class), eq(1L), eq(CartAction.PLUS));
    }

    @Test
    void addOrRemoveToCart_shouldUpdateCart_whenActionIsMinus() {
        when(cartService.updateItemCount(any(WebSession.class), eq(1L), eq(CartAction.MINUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "MINUS")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(cartService).updateItemCount(any(WebSession.class), eq(1L), eq(CartAction.MINUS));
    }

    @Test
    void updateCartFromCartPage_shouldUpdateAndReturnCartPage() {
        when(cartService.updateItemCount(any(WebSession.class), eq(1L), eq(CartAction.PLUS)))
                .thenReturn(Mono.empty());

        when(cartService.getCartItems(any(WebSession.class)))
                .thenReturn(Flux.empty());

        when(cartService.getCartTotal(any(WebSession.class)))
                .thenReturn(Mono.just(BigDecimal.ZERO));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(cartService).updateItemCount(any(WebSession.class), eq(1L), eq(CartAction.PLUS));
        verify(cartService).getCartItems(any(WebSession.class));
        verify(cartService).getCartTotal(any(WebSession.class));
    }
}