package ru.yandex.practicum.mymarket.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Request.CartActionRequestDto;
import ru.yandex.practicum.mymarket.service.CartService;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public Mono<Rendering> addOrRemoveItemInCart(@ModelAttribute @Valid CartActionRequestDto request,
                                                 WebSession session) {
        return cartService.updateItemCount(session.getId(), request.id(), request.action())
                .then(Mono.just(Rendering.redirectTo(
                        "/items?search=" + request.search() +
                                "&sort=" + request.sort() +
                                "&pageNumber=" + request.pageNumber() +
                                "&pageSize=" + request.pageSize()).build()));
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> addOrRemoveItemInCartById(
            @PathVariable(required = false) String id,
            @ModelAttribute @Valid CartActionRequestDto request,
            WebSession session
    ) {
        return cartService.updateItemCount(session.getId(), request.id(), request.action())
                .then(Mono.just(Rendering.redirectTo("/items/" + id).build()));
    }

    @GetMapping("/cart/items")
    public Mono<Rendering> getItems(WebSession session) {
        Flux<ItemDto> itemsFlux = cartService.getCartItems(session.getId())
                .map(cartItem -> ItemDto.from(cartItem.getItem(), cartItem.getQuantity()));

        return Mono.zip(
                itemsFlux.collectList(),
                cartService.getCartTotal(session.getId())
        ).map(tuple -> Rendering.view("cart")
                .modelAttribute("items", tuple.getT1())
                .modelAttribute("total", tuple.getT2())
                .build());
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> updateItems(@RequestParam Long id, @RequestParam CartAction action,
                                       Model model, WebSession session) {
        Flux<ItemDto> itemsFlux = cartService.updateItemCount(session.getId(), id, action)
                .thenMany(cartService.getCartItems(session.getId()))
                .map(cartItem -> ItemDto.from(cartItem.getItem(), cartItem.getQuantity()));

        return Mono.zip(
                itemsFlux.collectList(),
                cartService.getCartTotal(session.getId())
        ).map(tuple -> Rendering.view("cart")
                .modelAttribute("items", tuple.getT1())
                .modelAttribute("total", tuple.getT2())
                .build());
    }
}