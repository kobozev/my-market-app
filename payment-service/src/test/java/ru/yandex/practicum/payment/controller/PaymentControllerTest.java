package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.service.PaymentService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
    }

    @Test
    void getBalance_shouldReturnBalance() {

        BalanceResponse balanceResponse = new BalanceResponse()
                .userId(testUserId)
                .balance(1000.00);

        when(paymentService.getBalance(testUserId))
                .thenReturn(Mono.just(balanceResponse));

        webTestClient.get()
                .uri("/payments/balance/{userId}", testUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(1)
                .jsonPath("$.balance").isEqualTo(1000.00);

        verify(paymentService).getBalance(testUserId);
    }

    @Test
    void processPayment_shouldReturnSuccess_whenPaymentSucceeds() {

        PaymentResponse response = new PaymentResponse()
                .success(true)
                .newBalance(400.00);

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(response));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(100.00);

        webTestClient.post()
                .uri("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.newBalance").isEqualTo(400.00);

        verify(paymentService)
                .processPayment(any(PaymentRequest.class));
    }

    @Test
    void processPayment_shouldReturnFailure_whenInsufficientBalance() {

        PaymentResponse response = new PaymentResponse()
                .success(false)
                .newBalance(1000.00);

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(response));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(600.00);

        webTestClient.post()
                .uri("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(1000.00);

        verify(paymentService)
                .processPayment(any(PaymentRequest.class));
    }
}