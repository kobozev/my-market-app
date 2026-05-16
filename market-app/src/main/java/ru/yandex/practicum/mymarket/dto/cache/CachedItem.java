package ru.yandex.practicum.mymarket.dto.cache;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
@Getter
public class CachedItem {

    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    public CachedItem(Long id, String title, String description, BigDecimal price) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
    }

    public static CachedItem fromItem(Item item) {
        return new CachedItem(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getPrice()
        );
    }

}