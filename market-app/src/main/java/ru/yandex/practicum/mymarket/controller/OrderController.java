package ru.yandex.practicum.mymarket.controller;

import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.OrderProcessingService;
import ru.yandex.practicum.mymarket.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class OrderController {
    private final OrderService orderService;
    private final OrderProcessingService orderProcessingService;

    public OrderController(OrderService orderService, OrderProcessingService orderProcessingService) {
        this.orderService = orderService;
        this.orderProcessingService = orderProcessingService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getOrders() {
        return orderService.getAll()
                .map(OrderDto::from)
                .collectList()
                .map(orders -> Rendering.view("orders")
                        .modelAttribute("orders", orders)
                        .build());
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrderById(
            @PathVariable @Min(1) long id
    ) {
        return orderService.getById(id)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", OrderDto.from(order))
                        .build()
                )
                .switchIfEmpty(
                        Mono.just(Rendering.view("notfound").build())
                );
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy(WebSession session) {
        return orderProcessingService.checkout(session.getId())
                .map(order -> Rendering.redirectTo("/orders/" + order.getId() + "?newOrder=true").build())
                .onErrorResume(e -> {
                    String message = (e instanceof WebClientResponseException ex &&
                            ex.getStatusCode().is4xxClientError())
                            ? "Оплата не прошла. Недостаточно средств на балансе."
                            : "Произошла ошибка при оформлении заказа. Попробуйте позже.";
                    return Mono.just(Rendering.view("checkout-error")
                            .modelAttribute("errorMessage", message)
                            .build());
                });
    }
}