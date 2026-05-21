package ru.yandex.practicum.mymarket.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.request.CartActionRequestDto;
import ru.yandex.practicum.mymarket.model.SecurityUser;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.payment.client.api.PaymentsApi;

import java.math.BigDecimal;
import java.util.List;

@Controller
@Validated
public class CartController {

    private static final Logger log =
            LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final PaymentsApi paymentsApi;

    public CartController(
            CartService cartService,
            PaymentsApi paymentsApi
    ) {
        this.cartService = cartService;
        this.paymentsApi = paymentsApi;
    }

    @PostMapping("/items")
    public Mono<Rendering> addOrRemoveItemInCart(
            @ModelAttribute @Valid CartActionRequestDto request,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return cartService.updateItemCount(
                        principal.getId(),
                        request.id(),
                        request.action()
                )
                .thenReturn(
                        Rendering.redirectTo(
                                "/items?search=" + request.search()
                                        + "&sort=" + request.sort()
                                        + "&pageNumber=" + request.pageNumber()
                                        + "&pageSize=" + request.pageSize()
                        ).build()
                );
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> addOrRemoveItemInCartById(
            @PathVariable String id,
            @ModelAttribute @Valid CartActionRequestDto request,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return cartService.updateItemCount(
                        principal.getId(),
                        request.id(),
                        request.action()
                )
                .thenReturn(
                        Rendering.redirectTo("/items/" + id)
                                .build()
                );
    }

    @GetMapping("/cart/items")
    public Mono<Rendering> getItems(
            @AuthenticationPrincipal SecurityUser principal
    ) {

        Long userId = principal.getId();

        Flux<ItemDto> itemsFlux = cartService.getCartItems(userId)
                .map(cartItem ->
                        ItemDto.from(
                                cartItem.getItem(),
                                cartItem.getQuantity()
                        )
                );

        return Mono.zip(
                        itemsFlux.collectList(),
                        cartService.getCartTotal(userId),
                        paymentsApi.getBalance(userId)
                                .doOnNext(balance ->
                                        log.debug("Balance loaded for user {} from Get method", userId)
                                )
                                .doOnError(error ->
                                        log.error(
                                                "Error getting balance for user {}",
                                                userId,
                                                error
                                        )
                                )
                )
                .map(tuple ->
                        buildCartRendering(
                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3().getBalance()
                        )
                );
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> updateItems(
            @ModelAttribute @Valid CartActionRequestDto request,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        Long userId = principal.getId();

        Flux<ItemDto> itemsFlux = cartService.updateItemCount(
                        userId,
                        request.id(),
                        request.action()
                )
                .thenMany(cartService.getCartItems(userId))
                .map(cartItem ->
                        ItemDto.from(
                                cartItem.getItem(),
                                cartItem.getQuantity()
                        )
                );

        return Mono.zip(
                        itemsFlux.collectList(),
                        cartService.getCartTotal(userId),
                        paymentsApi.getBalance(userId)
                                .doOnNext(balance ->
                                        log.debug("Balance loaded for user {} from Post method", userId)
                                )
                                .doOnError(error ->
                                        log.error(
                                                "Error getting balance for user {}",
                                                userId,
                                                error
                                        )
                                )
                )
                .map(tuple ->
                        buildCartRendering(
                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3().getBalance()
                        )
                );
    }

    private Rendering buildCartRendering(
            List<ItemDto> items,
            BigDecimal total,
            BigDecimal balance
    ) {

        boolean canBuy =
                balance != null && balance.compareTo(total) >= 0;

        boolean insufficientFunds =
                balance != null
                        && !canBuy
                        && !items.isEmpty();

        return Rendering.view("cart")
                .modelAttribute("items", items)
                .modelAttribute("total", total)
                .modelAttribute("canBuy", canBuy)
                .modelAttribute("insufficientFunds", insufficientFunds)
                .build();
    }
}