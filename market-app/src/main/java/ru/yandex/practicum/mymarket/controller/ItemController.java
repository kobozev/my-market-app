package ru.yandex.practicum.mymarket.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.dto.request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.SecurityUser;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

@Controller
@Validated
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(
            ItemService itemService,
            CartService cartService
    ) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping(value = {"/", "/items"})
    public Mono<Rendering> getItems(
            @ModelAttribute ItemsQueryRequestDto queryParams,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return itemService.getAllItems(queryParams)
                .zipWith(getCartOrEmpty(principal))
                .map(tuple -> {

                    var page = tuple.getT1();
                    var cart = tuple.getT2();

                    var paging = new PagingDto(
                            page.getNumber() + 1,
                            page.getSize(),
                            page.hasPrevious(),
                            page.hasNext()
                    );

                    var items = page.getContent().stream()
                            .map(item ->
                                    ItemDto.from(
                                            item,
                                            cart.getItemCountById(item.getId())
                                    )
                            )
                            .toList();

                    return Rendering.view("items")
                            .modelAttribute("items", items)
                            .modelAttribute("paging", paging)
                            .modelAttribute(
                                    "search",
                                    queryParams.getSearch() != null
                                            ? queryParams.getSearch()
                                            : ""
                            )
                            .modelAttribute(
                                    "sort",
                                    queryParams.getSort() != null
                                            ? queryParams.getSort()
                                            : ""
                            )
                            .build();
                });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(
            @PathVariable("id") long id,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        return itemService.getById(id)
                .zipWith(getCartOrEmpty(principal))
                .map(tuple ->
                        ItemDto.from(
                                tuple.getT1(),
                                tuple.getT2()
                                        .getItemCountById(tuple.getT1().getId())
                        )
                )
                .map(itemDto ->
                        Rendering.view("item")
                                .modelAttribute("item", itemDto)
                                .build()
                )
                .switchIfEmpty(
                        Mono.just(
                                Rendering.view("notfound")
                                        .build()
                        )
                );
    }

    private Mono<Cart> getCartOrEmpty(SecurityUser principal) {

        return principal != null
                ? cartService.getCart(principal.getId())
                : Mono.just(new Cart());
    }
}