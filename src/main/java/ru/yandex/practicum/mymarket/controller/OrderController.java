package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.OrderDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderService;
import jakarta.validation.constraints.Min;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/orders")
    public Mono<Rendering> getOrders() {
        return Mono.just(
                Rendering.view("orders")
                        .modelAttribute("orders", orderService.getAll().map(OrderDto::from))
                        .build()
        );
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrderById(@PathVariable @Min(1) long id, @RequestParam(required = false) String newOrder, Model model) {
        return orderService.getById(id)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", OrderDto.from(order)).build())
                .switchIfEmpty(Mono.just(Rendering.view("notfound").build()));
    }

    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        return cartService.getCartItems(session)
                .collectList()
                .flatMap(orderService::create)
                .flatMap(order -> cartService.clear(session)
                        .thenReturn("redirect:orders/" + order.getId() + "?newOrder=true"));
    }
}