package ru.yandex.practicum.mymarket.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.model.Item;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item, int count) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                count
        );
    }
}