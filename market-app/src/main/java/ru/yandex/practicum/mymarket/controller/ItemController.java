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
    public Mono<Rendering> getItems(@ModelAttribute ItemsQueryRequestDto queryParams,
                                    WebSession session) {

        return itemService.getAllItems(queryParams)
                .zipWith(cartService.getCart(session.getId()))
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
                            .map(item -> ItemDto.from(
                                    item,
                                    cart.getItemCountById(item.getId())
                            ))
                            .toList();

                    return Rendering.view("items")
                            .modelAttribute("items", items)
                            .modelAttribute("paging", paging)
                            .modelAttribute("search",
                                    queryParams.getSearch() != null ? queryParams.getSearch() : "")
                            .modelAttribute("sort",
                                    queryParams.getSort() != null ? queryParams.getSort() : "")
                            .build();
                });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable("id") long id,
                                   WebSession session) {

        return itemService.getById(id)
                .flatMap(item ->
                        cartService.getCart(session.getId())
                                .map(cart -> ItemDto.from(
                                        item,
                                        cart.getItemCountById(item.getId())
                                ))
                )
                .map(itemDto -> Rendering.view("item")
                        .modelAttribute("item", itemDto)
                        .build())
                .switchIfEmpty(Mono.just(
                        Rendering.view("notfound").build()
                ));
    }
}