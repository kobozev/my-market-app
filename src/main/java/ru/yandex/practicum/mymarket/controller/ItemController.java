package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import ru.yandex.practicum.mymarket.constants.SortType;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.dto.PagingDto;
import ru.yandex.practicum.mymarket.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;
    private final ItemMapper mapper;

    @GetMapping({"/", "/items"})
    public String items(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize,
            @SessionAttribute(required = false) Long cartId,
            Model model
    ) {
        if (cartId == null) cartId = 1L;

        Pageable pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                resolveSort(sort)
        );

        Page<Item> page = itemService.find(search, pageable);

        // получаем текущую корзину
        Map<Long, Integer> cartMap = cartService.getCart(cartId)
                .items()
                .stream()
                .collect(Collectors.toMap(
                        ItemDto::id,
                        ItemDto::count
                ));

        // мапим в DTO с учётом количества в корзине
        List<ItemDto> items = page.getContent().stream()
                .map(item -> mapper.toDto(
                        item,
                        cartMap.getOrDefault(item.getId(), 0)
                ))
                .toList();

        // группировка по 3 элемента
        List<List<ItemDto>> grid = toGrid(items, 3);

        model.addAttribute("items", grid);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("paging", buildPaging(page, pageNumber, pageSize));

        return "items";
    }

    private Sort resolveSort(SortType sort) {
        return switch (sort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            default -> Sort.unsorted();
        };
    }

    // группировка товаров по строкам (по 3)
    private List<List<ItemDto>> toGrid(List<ItemDto> items, int size) {
        List<List<ItemDto>> result = new ArrayList<>();

        for (int i = 0; i < items.size(); i += size) {
            List<ItemDto> row = new ArrayList<>(
                    items.subList(i, Math.min(i + size, items.size()))
            );

            // добавляем "заглушки"
            while (row.size() < size) {
                row.add(emptyItem());
            }

            result.add(row);
        }

        return result;
    }

    private ItemDto emptyItem() {
        return new ItemDto(
                -1L,
                "",
                "",
                "",
                0L,
                0
        );
    }

    private PagingDto buildPaging(Page<Item> page, int pageNumber, int pageSize) {
        return new PagingDto(
                pageSize,
                pageNumber,
                pageNumber > 1,
                page.hasNext()
        );
    }
}