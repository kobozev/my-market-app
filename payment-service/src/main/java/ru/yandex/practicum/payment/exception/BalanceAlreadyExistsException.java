package ru.yandex.practicum.payment.exception;

public class BalanceAlreadyExistsException extends RuntimeException {

    public BalanceAlreadyExistsException(Long userId) {
        super("Balance already exists for user " + userId);
    }
}
