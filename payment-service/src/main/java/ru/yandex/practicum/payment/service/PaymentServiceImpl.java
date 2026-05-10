package ru.yandex.practicum.payment.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final AtomicReference<Double> balance =
            new AtomicReference<>(0.0);

    @Override
    public Mono<BalanceResponse> getBalance(Long userId) {

        BalanceResponse response = new BalanceResponse();

        response.setUserId(userId);
        response.setBalance(balance.get());

        return Mono.just(response);
    }

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {

        synchronized (this) {

            Double currentBalance = balance.get();

            if (currentBalance < request.getAmount()) {

                PaymentResponse response = new PaymentResponse();

                response.setSuccess(false);
                response.setNewBalance(currentBalance);

                return Mono.just(response);
            }

            Double newBalance =
                    currentBalance - request.getAmount();

            balance.set(newBalance);

            PaymentResponse response = new PaymentResponse();

            response.setSuccess(true);
            response.setNewBalance(newBalance);

            return Mono.just(response);
        }
    }
}