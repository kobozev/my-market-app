package ru.yandex.practicum.mymarket.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.constants.SortType;

public record CartActionRequestDto(
        @NotNull(message = "Item ID required")
        @Min(value = 1, message = "Item ID must be positive")
        Long id,
        @NotNull(message = "Action required")
        CartAction action,
        String search,
        SortType sort,
        Integer pageNumber,
        Integer pageSize
) {
    public CartActionRequestDto(Long id, CartAction action, String search, SortType sort, Integer pageNumber, Integer pageSize) {
        this.id = id;
        this.action = action;
        this.search = search != null ? search : "";
        this.sort = sort != null ? sort : SortType.NO;
        this.pageNumber = pageNumber != null ? pageNumber : 1;
        this.pageSize = pageSize != null ? pageSize : 5;
    }
}