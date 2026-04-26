package ru.yandex.practicum.mymarket.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("items")
public class Item {
    @Id
    private Long id;
    private String title;
    private String description;
    private String imgPath;
    private BigDecimal price;

    @Column
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime updatedAt;

    protected Item() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImgPath() {
        return imgPath;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {
        private final Item item = new Item();

        public Builder id(Long id) {
            item.setId(id);
            return this;
        }

        public Builder title(String title) {
            item.setTitle(title);
            return this;
        }

        public Builder description(String description) {
            item.setDescription(description);
            return this;
        }

        public Builder imgPath(String imgPath) {
            item.setImgPath(imgPath);
            return this;
        }

        public Builder price(BigDecimal price) {
            item.setPrice(price);
            return this;
        }

        public Item build() {
            return item;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}