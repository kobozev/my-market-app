package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.service.OrderProcessingService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
@Import(TestViewConfig.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderProcessingService orderProcessingService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setOrderItems(List.of());
    }

    @Test
    void getOrders_shouldDisplayOrdersPage() {
        when(orderService.getAll())
                .thenReturn(Flux.just(testOrder));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getAll();
    }

    @Test
    void getOrderById_shouldDisplayOrderPage_whenOrderExists() {
        when(orderService.getById(1L))
                .thenReturn(Mono.just(testOrder));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getById(1L);
    }

    @Test
    void getOrderById_shouldReturnNotFoundView_whenOrderDoesNotExist() {
        when(orderService.getById(999L))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/orders/999")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getById(999L);
    }

    @Test
    void getOrderById_shouldAcceptNewOrderParameter() {
        when(orderService.getById(1L))
                .thenReturn(Mono.just(testOrder));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/1")
                        .queryParam("newOrder", "true")
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getById(1L);
    }

    @Test
    void buy_shouldCheckoutAndRedirect() {
        when(orderProcessingService.checkout(anyString()))
                .thenReturn(Mono.just(testOrder));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/1?newOrder=true");

        verify(orderProcessingService).checkout(anyString());
    }

    @Test
    void buy_shouldShowErrorPage_whenPaymentFails() {
        when(orderProcessingService.checkout(anyString()))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad request",
                                null,
                                null,
                                null
                        )
                ));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().isOk();

        verify(orderProcessingService).checkout(anyString());
    }

    @Test
    void buy_shouldShowErrorPage_whenUnexpectedError() {
        when(orderProcessingService.checkout(anyString()))
                .thenReturn(Mono.error(
                        new RuntimeException("Connection refused")
                ));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().isOk();

        verify(orderProcessingService).checkout(anyString());
    }
}