package ru.yandex.practicum.mymarket.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Table("carts")
public class Cart {
    @Id
    private Long id;

    @Column("session_id")
    private String sessionId;

    @Transient
    private List<CartItem> items = new ArrayList<>();

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    public int getItemCountById(Long itemId) {
        return items.stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst()
                .map(CartItem::getQuantity)
                .orElse(0);
    }
}
