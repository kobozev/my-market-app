package ru.yandex.practicum.mymarket.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Table("cart_items")
public class CartItem {
    @Getter
    @Id
    private Long id;

    @Getter
    @Column("cart_id")
    private Long cartId;

    @Getter
    @Column("item_id")
    private Long itemId;

    @Getter
    private int  quantity;

    @Transient
    private Cart cart;

    @Transient
    private Item item;

    @Getter
    @Column("created_at")
    private LocalDateTime createdAt;

    @Getter
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    public BigDecimal getSubtotal() {
        if (item == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(new BigDecimal(quantity));
    }

    @JsonIgnore
    public Cart getCart() {
        return cart;
    }

    @JsonIgnore
    public Item getItem() {
        return item;
    }

}
