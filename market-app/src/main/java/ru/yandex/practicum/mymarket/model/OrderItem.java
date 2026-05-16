package ru.yandex.practicum.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "order_items")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    private Long id;

    @Column("order_id")
    private Long orderId;

    @Column("item_id")
    private Long itemId;

    @Transient
    private Order order;

    @Transient
    private Item item;

    private int quantity;

    private BigDecimal price;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.itemId = item.getId();
        this.quantity = quantity;
        this.price = item.getPrice();
    }

    public void setOrder(Order order) {
        this.order = order;
        this.orderId = order.getId();
    }

    public void setItem(Item item) {
        this.item = item;
        this.itemId = item.getId();
    }
}