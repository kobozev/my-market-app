package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.TestSecurityConfig;
import ru.yandex.practicum.mymarket.config.TestViewConfig;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.service.OrderProcessingService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
@Import({
        TestViewConfig.class,
        TestSecurityConfig.class
})
class OrderControllerTest extends BaseControllerTest {

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

        when(orderService.getAll(anyLong()))
                .thenReturn(Flux.just(testOrder));

        authenticatedClient().get()
                .uri("/orders")
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderService)
                .getAll(anyLong());
    }

    @Test
    void getOrderById_shouldDisplayOrderPage_whenOrderExists() {

        when(orderService.getById(eq(1L), anyLong()))
                .thenReturn(Mono.just(testOrder));

        authenticatedClient().get()
                .uri("/orders/1")
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderService)
                .getById(eq(1L), anyLong());
    }

    @Test
    void getOrderById_shouldReturnNotFoundView_whenOrderDoesNotExist() {

        when(orderService.getById(eq(999L), anyLong()))
                .thenReturn(Mono.empty());

        authenticatedClient().get()
                .uri("/orders/999")
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderService)
                .getById(eq(999L), anyLong());
    }

    @Test
    void getOrderById_shouldAcceptNewOrderParameter() {

        when(orderService.getById(eq(1L), anyLong()))
                .thenReturn(Mono.just(testOrder));

        authenticatedClient().get()
                .uri(uriBuilder -> uriBuilder
                        .path("/orders/1")
                        .queryParam("newOrder", "true")
                        .build())
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderService)
                .getById(eq(1L), anyLong());
    }

    @Test
    void buy_shouldCheckoutAndRedirect() {

        when(orderProcessingService.checkout(anyLong()))
                .thenReturn(Mono.just(testOrder));

        authenticatedClient().post()
                .uri("/buy")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .location("/orders/1?newOrder=true");

        verify(orderProcessingService)
                .checkout(anyLong());
    }

    @Test
    void buy_shouldShowErrorPage_whenPaymentFails() {

        when(orderProcessingService.checkout(anyLong()))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                null,
                                null,
                                null
                        )
                ));

        authenticatedClient().post()
                .uri("/buy")
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderProcessingService)
                .checkout(anyLong());
    }

    @Test
    void buy_shouldShowErrorPage_whenUnexpectedError() {

        when(orderProcessingService.checkout(anyLong()))
                .thenReturn(Mono.error(
                        new RuntimeException("Connection refused")
                ));

        authenticatedClient().post()
                .uri("/buy")
                .exchange()
                .expectStatus()
                .isOk();

        verify(orderProcessingService)
                .checkout(anyLong());
    }
}