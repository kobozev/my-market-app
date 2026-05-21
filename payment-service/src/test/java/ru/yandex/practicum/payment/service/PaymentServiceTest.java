package ru.yandex.practicum.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.payment.model.Balance;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.repository.PaymentRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Long testUserId;

    private Balance testBalance;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testBalance = new Balance(testUserId, 1000.00);
    }

    @Test
    void getBalance_shouldReturnCurrentBalance() {
        when(paymentRepository.findById(testUserId))
                .thenReturn(Mono.just(testBalance));

        StepVerifier.create(paymentService.getBalance(testUserId))
                .assertNext(balance -> {

                    assertEquals(testUserId, balance.getUserId());

                    assertEquals(
                            1000.00,
                            balance.getBalance()
                    );
                })
                .verifyComplete();

    }

    @Test
    void processPayment_shouldSucceed_whenSufficientBalance() {
        when(paymentRepository.findById(testUserId))
                .thenReturn(Mono.just(testBalance));

        when(paymentRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(100.00);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(response -> {

                    assertTrue(response.getSuccess());

                    assertEquals(
                            900.00,
                            response.getNewBalance()
                    );
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldFail_whenInsufficientBalance() {
        when(paymentRepository.findById(testUserId))
                .thenReturn(Mono.just(testBalance));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(2000.00);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(response -> {

                    assertFalse(response.getSuccess());

                    assertEquals(
                            1000.00,
                            response.getNewBalance()
                    );
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldSucceed_whenExactBalance() {
        when(paymentRepository.findById(testUserId))
                .thenReturn(Mono.just(testBalance));

        when(paymentRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(1000.00);

        StepVerifier.create(paymentService.processPayment(request))
                .assertNext(response -> {

                    assertTrue(response.getSuccess());

                    assertEquals(
                            0.00,
                            response.getNewBalance()
                    );
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldUpdateBalanceAfterSuccessfulPayment() {
        when(paymentRepository.findById(testUserId))
                .thenReturn(Mono.just(testBalance));

        when(paymentRepository.save(any()))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(300.00);

        StepVerifier.create(paymentService.processPayment(request))
                .expectNextMatches(response ->
                        Boolean.TRUE.equals(response.getSuccess())
                                && Double.valueOf(700.00)
                                .equals(response.getNewBalance())
                )
                .verifyComplete();

        StepVerifier.create(paymentService.getBalance(testUserId))
                .assertNext(balance ->
                        assertEquals(
                                700.00,
                                balance.getBalance()
                        )
                )
                .verifyComplete();
    }
}