package ru.yandex.practicum.mymarket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("items")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    private Long id;

    private String title;

    private String description;

    private String imgPath;

    private BigDecimal price;

    private Integer stockQuantity;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}