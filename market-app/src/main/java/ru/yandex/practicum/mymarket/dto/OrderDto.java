package ru.yandex.practicum.mymarket.dto;

import ru.yandex.practicum.mymarket.model.Order;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        long id,
        List<CartItemDto> items,
        BigDecimal totalSum
) {
    public static OrderDto from(Order order) {
        if (order == null) {
            return null;
        }

        var items = order.getOrderItems().stream()
                .map(oi -> CartItemDto.from(oi.getItem(), oi.getQuantity()))
                .toList();

        var totalSum = items.stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderDto(
                order.getId() == null ? 0L : order.getId(),
                items,
                totalSum
        );
    }
}
