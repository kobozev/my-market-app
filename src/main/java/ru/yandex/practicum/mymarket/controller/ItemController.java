package ru.yandex.practicum.mymarket.controller;

import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.Request.ItemsQueryRequestDto;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@Validated
public class ItemController {
    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping(value = {"/", "/items"})
    public Mono<Rendering> getItems(@ModelAttribute ItemsQueryRequestDto queryParams, WebSession session) {
        return itemService.getAllItems(queryParams)
                .zipWith(cartService.getCart(session))
                .map(tuple -> {
                    var page = tuple.getT1();
                    var currentPaging = new PagingDto(
                            page.getNumber() + 1,
                            page.getSize(),
                            page.hasPrevious(),
                            page.hasNext()
                    );
                    var cart = tuple.getT2();

                    var items = page.getContent().stream()
                            .map(item -> ItemDto.from(item, cart.getItemCountById(item.getId()))) // or get count from cart
                            .toList();
                    return Rendering.view("items")
                            .modelAttribute("items", items)
                            .modelAttribute("paging", currentPaging)
                            .modelAttribute("search", queryParams.getSearch())
                            .modelAttribute("sort", queryParams.getSort())
                            .build();
                });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable("id") long id, WebSession session) {
        return itemService.getById(id)
                .zipWith(cartService.getCart(session))
                .map(tuple -> ItemDto.from(tuple.getT1(),
                        tuple.getT2().getItemCountById(tuple.getT1().getId())))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build())
                .switchIfEmpty(Mono.just(Rendering.view("/notfound").build()));
    }
}