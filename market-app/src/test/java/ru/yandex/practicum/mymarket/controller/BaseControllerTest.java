package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.mymarket.config.TestSecurityConfig;
import ru.yandex.practicum.mymarket.model.SecurityUser;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.mymarket.service.UserService;
import ru.yandex.practicum.payment.client.api.PaymentsApi;

@Import(TestSecurityConfig.class)
public abstract class BaseControllerTest {

    @Autowired
    protected WebTestClient webTestClient;

    @BeforeEach
    void clearSecurityContext() {
        TestSecurityConfig.clearContext();
    }

    @MockitoBean
    protected CartService cartService;

    @MockitoBean
    protected OrderService orderService;

    @MockitoBean
    protected ItemService itemService;

    @MockitoBean
    protected PaymentsApi paymentsApi;

    @MockitoBean
    protected UserService userService;

    protected SecurityUser mockSecurityUser() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .enabled(true)
                .build();

        return new SecurityUser(user);
    }

    protected UsernamePasswordAuthenticationToken createMockAuthentication() {
        SecurityUser user = mockSecurityUser();
        return UsernamePasswordAuthenticationToken.authenticated(
                user, user.getPassword(), user.getAuthorities());
    }

    protected WebTestClient authenticatedClient() {
        TestSecurityConfig.setContext(
                new SecurityContextImpl(createMockAuthentication()));
        return webTestClient;
    }
}
