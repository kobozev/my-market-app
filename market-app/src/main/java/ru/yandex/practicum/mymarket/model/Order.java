package ru.yandex.practicum.mymarket.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Table(name = "orders")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Transient
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

}