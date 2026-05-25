package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.AuthTestSecurityConfig;
import ru.yandex.practicum.mymarket.config.TestViewConfig;
import ru.yandex.practicum.mymarket.model.User;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(AuthController.class)
@Import({
        TestViewConfig.class,
        AuthTestSecurityConfig.class
})
class AuthControllerTest extends BaseControllerTest {

    @Test
    void loginPage_shouldReturnLoginView() {
        webTestClient.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginPage_shouldPassErrorFlag_whenErrorParam() {
        webTestClient.get()
                .uri("/login?error")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginPage_shouldPassRegisteredFlag_whenRegisteredParam() {
        webTestClient.get()
                .uri("/login?registered")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void registerPage_shouldReturnRegisterView() {
        webTestClient.get()
                .uri("/register")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void register_shouldRedirectToItems_whenSuccessful() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .enabled(true)
                .build();

        when(userService.registerUser(eq("newuser"), eq("Pass1!ab")))
                .thenReturn(Mono.just(user));
        when(userService.loginUser(any(User.class), any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/register")
                .bodyValue("username=newuser&password=Pass1!ab")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/items");

        verify(userService).registerUser(eq("newuser"), eq("Pass1!ab"));
        verify(userService).loginUser(any(User.class), any(ServerWebExchange.class));
    }

    @Test
    void register_shouldShowRegisterPage_whenRegistrationFails() {
        when(userService.registerUser(eq("existing"), eq("Pass1!ab")))
                .thenReturn(Mono.error(new RuntimeException("Имя пользователя уже занято")));

        webTestClient.post()
                .uri("/register")
                .bodyValue("username=existing&password=Pass1!ab")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().isOk();

        verify(userService).registerUser(eq("existing"), eq("Pass1!ab"));
    }

    @Test
    void register_shouldShowRegisterPage_whenPasswordLacksComplexity() {
        webTestClient.post()
                .uri("/register")
                .bodyValue("username=newuser&password=newpassword")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().isOk();

        verify(userService, never()).registerUser(any(), any());
    }

    @Test
    void register_shouldShowRegisterPage_whenUsernameBlank() {
        webTestClient.post()
                .uri("/register")
                .bodyValue("username=&password=newpassword")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus().isOk();

        verify(userService, never()).registerUser(any(), any());
    }
}
