package ru.yandex.practicum.mymarket.dto;

import ru.yandex.practicum.mymarket.model.Order;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        long id,
        List<ItemDto> items,
        BigDecimal totalSum
) {
    public static OrderDto from(Order order) {
        if (order == null) {
            return null;
        }

        var orderItems = order.getOrderItems();
        var totalSum = orderItems.stream()
                .map(x -> x.getPrice().multiply(new BigDecimal(x.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var items = orderItems.stream()
                .map(orderItem-> ItemDto.from(orderItem, orderItem.getQuantity())).toList();
        return new OrderDto(order.getId() == null ? 0L : order.getId(), items, totalSum);
    }
}
