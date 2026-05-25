package ru.yandex.practicum.payment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.PaymentsApi;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.CreateBalanceRequest;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.exception.BalanceAlreadyExistsException;
import ru.yandex.practicum.payment.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> createBalance(
            Mono<CreateBalanceRequest> createBalanceRequest,
            ServerWebExchange exchange
    ) {

        return createBalanceRequest
                .flatMap(request ->
                        paymentService.createBalance(request.getUserId())
                )
                .map(this::toBalanceResponse)
                .map(response ->
                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(response)
                )
                .onErrorResume(
                        BalanceAlreadyExistsException.class,
                        ex -> Mono.just(
                                ResponseEntity.status(HttpStatus.CONFLICT)
                                        .build()
                        )
                );
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

    private BalanceResponse toBalanceResponse(Balance balance) {

        return new BalanceResponse()
                .userId(balance.getUserId())
                .balance(balance.getBalance());
    }
}