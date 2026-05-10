package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Mono<BalanceResponse> getBalance(Long userId) {

        return paymentRepository.findById(userId)
                .map(balance -> new BalanceResponse()
                        .userId(balance.getUserId())
                        .balance(balance.getBalance()))
                .defaultIfEmpty(
                        new BalanceResponse()
                                .userId(userId)
                                .balance(0.0)
                );
    }

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {

        return paymentRepository.findById(request.getUserId())
                .flatMap(balance -> {

                    double currentBalance = balance.getBalance();
                    double paymentAmount = request.getAmount();

                    if (currentBalance < paymentAmount) {
                        return Mono.just(
                                new PaymentResponse()
                                        .success(false)
                                        .newBalance(currentBalance)
                        );
                    }

                    double updatedBalance = currentBalance - paymentAmount;

                    balance.setBalance(updatedBalance);

                    return paymentRepository.save(balance)
                            .map(saved -> new PaymentResponse()
                                    .success(true)
                                    .newBalance(saved.getBalance()));
                })
                .switchIfEmpty(
                        Mono.just(
                                new PaymentResponse()
                                        .success(false)
                                        .newBalance(0.0)
                        )
                );
    }
}