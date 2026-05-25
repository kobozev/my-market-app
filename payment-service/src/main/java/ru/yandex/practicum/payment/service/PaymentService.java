package ru.yandex.practicum.payment.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;

public interface PaymentService {

    Mono<BalanceResponse> getBalance(Long userId);

    Mono<Balance> createBalance(Long userId);

    Mono<PaymentResponse> processPayment(PaymentRequest request);
}