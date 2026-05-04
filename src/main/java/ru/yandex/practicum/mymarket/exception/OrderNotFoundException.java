package ru.yandex.practicum.mymarket.exception;

public class OrderNotFoundException extends RuntimeException {
    private final Long orderId;

    public OrderNotFoundException(Long orderId) {
        super("Заказ с id=" + orderId + " не найден");
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}