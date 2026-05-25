package ru.yandex.practicum.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.exception.BalanceAlreadyExistsException;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Mono<BalanceResponse> getBalance(Long userId) {

        return paymentRepository.findById(userId)
                .map(balance -> new BalanceResponse()
                        .userId(balance.getUserId())
                        .balance(balance.getBalance())
                )
                .defaultIfEmpty(
                        new BalanceResponse()
                                .userId(userId)
                                .balance(BigDecimal.ZERO)
                );
    }

    @Override
    public Mono<Balance> createBalance(Long userId) {

        return paymentRepository.findById(userId)

                .flatMap(existing ->
                        Mono.<Balance>error(
                                new BalanceAlreadyExistsException(userId)
                        )
                )

                .switchIfEmpty(Mono.defer(() -> {

                    BigDecimal amount = BigDecimal.ZERO;

                    Balance balance = new Balance();
                    balance.setUserId(userId);
                    balance.setBalance(amount);

                    return paymentRepository.save(balance);
                }));
    }

    @Override
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {

        return paymentRepository.findById(request.getUserId())

                .flatMap(balance -> {

                    BigDecimal currentBalance = balance.getBalance();

                    BigDecimal paymentAmount =
                            request.getAmount();

                    if (currentBalance.compareTo(paymentAmount) < 0) {

                        return Mono.just(
                                new PaymentResponse()
                                        .success(false)
                                        .newBalance(currentBalance)
                        );
                    }

                    BigDecimal updatedBalance =
                            currentBalance.subtract(paymentAmount);

                    balance.setBalance(updatedBalance);

                    return paymentRepository.save(balance)
                            .map(saved ->
                                    new PaymentResponse()
                                            .success(true)
                                            .newBalance(saved.getBalance())
                            );
                })

                .switchIfEmpty(
                        Mono.just(
                                new PaymentResponse()
                                        .success(false)
                                        .newBalance(BigDecimal.ZERO)
                        )
                );
    }


}