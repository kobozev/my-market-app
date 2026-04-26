package ru.yandex.practicum.mymarket.dto;

import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.math.BigDecimal;

public record ItemDto(
    long id,
    String title,
    String description,
    String imgPath,
    BigDecimal price,
    int count
) {
    public static ItemDto from(Item item, int count) {
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), count);
    }

    public static ItemDto from(OrderItem orderItem, int count) {
        var item = orderItem.getItem();
        return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getImgPath(), item.getPrice(), count);
    }
}
