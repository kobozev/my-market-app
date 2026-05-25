package ru.yandex.practicum.mymarket.controller;

import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.model.SecurityUser;
import ru.yandex.practicum.mymarket.service.OrderProcessingService;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
@Validated
public class OrderController {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final OrderProcessingService orderProcessingService;

    public OrderController(OrderService orderService,
                           OrderProcessingService orderProcessingService) {
        this.orderService = orderService;
        this.orderProcessingService = orderProcessingService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getOrders(
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return Mono.just(
                Rendering.view("orders")
                        .modelAttribute(
                                "orders",
                                orderService.getAll(principal.getId())
                                        .map(OrderDto::from)
                        )
                        .build()
        );
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrderById(
            @PathVariable @Min(1) long id,
            @RequestParam(required = false) String newOrder,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return orderService.getById(id, principal.getId())
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", OrderDto.from(order))
                        .modelAttribute("newOrder", newOrder != null)
                        .build()
                )
                .switchIfEmpty(
                        Mono.just(Rendering.view("notfound").build())
                );
    }

    @PostMapping("/buy")
    public Mono<Rendering> buy(
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return orderProcessingService.checkout(principal.getId())
                .map(order ->
                        Rendering.redirectTo(
                                "/orders/" + order.getId() + "?newOrder=true"
                        ).build()
                )
                .onErrorResume(e -> {

                    logger.error(
                            "Checkout failed for user {}: {}",
                            principal.getId(),
                            e.getMessage()
                    );

                    String message =
                            (e instanceof WebClientResponseException ex
                                    && ex.getStatusCode().is4xxClientError())
                                    ? "Оплата не прошла. Недостаточно средств на балансе."
                                    : "Произошла ошибка при оформлении заказа. Попробуйте позже.";

                    return Mono.just(
                            Rendering.view("checkout-error")
                                    .modelAttribute("errorMessage", message)
                                    .build()
                    );
                });
    }
}