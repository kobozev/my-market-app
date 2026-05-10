package ru.yandex.practicum.payment.controller;

import ru.yandex.practicum.payment.api.PaymentsApi;

import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(
            Long userId,
            ServerWebExchange exchange
    ) {

        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(
            Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange
    ) {

        return paymentRequest
                .flatMap(paymentService::processPayment)
                .map(ResponseEntity::ok);
    }
}
