package ru.yandex.practicum.payment.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.config.TestPaymentSecurityConfig;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.PaymentRequest;
import ru.yandex.practicum.payment.model.PaymentResponse;
import ru.yandex.practicum.payment.service.PaymentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(PaymentController.class)
@Import(TestPaymentSecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PaymentService paymentService;

    private Long testUserId;

    @BeforeEach
    void setUp() {

        testUserId = 1L;

        TestPaymentSecurityConfig.setContext(
                new SecurityContextImpl(mockJwtAuthentication())
        );
    }

    @AfterEach
    void tearDown() {
        TestPaymentSecurityConfig.clearContext();
    }

    @Test
    void getBalance_shouldReturnBalance_whenAuthorized() {

        BalanceResponse response = new BalanceResponse()
                .userId(testUserId)
                .balance(BigDecimal.valueOf(500.00));

        when(paymentService.getBalance(testUserId))
                .thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/payments/balance/{userId}", testUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo(testUserId)
                .jsonPath("$.balance").isEqualTo(500.00);

        verify(paymentService).getBalance(testUserId);
    }

    @Test
    void getBalance_shouldReturn401_whenNotAuthenticated() {

        TestPaymentSecurityConfig.clearContext();

        webTestClient.get()
                .uri("/payments/balance/{userId}", testUserId)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(paymentService, never()).getBalance(any());
    }

    @Test
    void processPayment_shouldReturnSuccess_whenAuthorized() {

        PaymentResponse response = new PaymentResponse()
                .success(true)
                .newBalance(BigDecimal.valueOf(400.00));

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(response));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(BigDecimal.valueOf(100.00));

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
    void processPayment_shouldReturn401_whenNotAuthenticated() {

        TestPaymentSecurityConfig.clearContext();

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(BigDecimal.valueOf(100.00));

        webTestClient.post()
                .uri("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();

        verify(paymentService, never())
                .processPayment(any());
    }

    @Test
    void processPayment_shouldReturn403_whenNoAuthority() {

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "payment-client")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        JwtAuthenticationToken token =
                new JwtAuthenticationToken(jwt, List.of());

        TestPaymentSecurityConfig.setContext(
                new SecurityContextImpl(token)
        );

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(BigDecimal.valueOf(100.00));

        webTestClient.post()
                .uri("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();

        verify(paymentService, never())
                .processPayment(any());
    }

    @Test
    void processPayment_shouldReturnFailure_whenInsufficientBalance() {

        PaymentResponse response = new PaymentResponse()
                .success(false)
                .newBalance(BigDecimal.valueOf(500.00));

        when(paymentService.processPayment(any(PaymentRequest.class)))
                .thenReturn(Mono.just(response));

        PaymentRequest request = new PaymentRequest()
                .userId(testUserId)
                .amount(BigDecimal.valueOf(600.00));

        webTestClient.post()
                .uri("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.newBalance").isEqualTo(500.00);

        verify(paymentService)
                .processPayment(any(PaymentRequest.class));
    }

    private JwtAuthenticationToken mockJwtAuthentication() {

        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "payment-client")
                .claim(
                        "resource_access",
                        java.util.Map.of(
                                "payment-service",
                                java.util.Map.of(
                                        "roles",
                                        List.of("payment.balance.manage")
                                )
                        )
                )
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();

        return new JwtAuthenticationToken(
                jwt,
                List.of(
                        new SimpleGrantedAuthority(
                                "payment.balance.manage"
                        )
                )
        );
    }
}