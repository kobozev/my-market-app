package ru.yandex.practicum.mymarket.dto;

import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;

public record CartItemDto(Item item, int quantity) {
    public BigDecimal getSubtotal() {
        return item.getPrice().multiply(new BigDecimal(quantity));
    }
}
