package ru.yandex.practicum.mymarket.controller;

import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.request.RegisterRequest;
import ru.yandex.practicum.mymarket.service.UserService;

import java.util.Objects;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public Mono<Rendering> loginPage(ServerWebExchange exchange) {

        var params = exchange.getRequest().getQueryParams();

        return Mono.just(
                Rendering.view("login")
                        .modelAttribute("hasError", params.containsKey("error"))
                        .modelAttribute("hasRegistered", params.containsKey("registered"))
                        .build()
        );
    }

    @GetMapping("/register")
    public Mono<Rendering> registerPage() {

        return Mono.just(
                Rendering.view("register")
                        .build()
        );
    }

    @PostMapping("/register")
    public Mono<Rendering> register(
            @ModelAttribute @Valid RegisterRequest request,
            BindingResult bindingResult,
            ServerWebExchange exchange
    ) {

        if (bindingResult.hasErrors()) {

            String message = bindingResult.getFieldErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("Некорректные данные");

            return Mono.just(
                    Rendering.view("register")
                            .modelAttribute("error", message)
                            .build()
            );
        }

        return userService.registerUser(request.username(), request.password())
                .flatMap(user -> userService.loginUser(user, exchange))
                .thenReturn(
                        Rendering.redirectTo("/items").build()
                )
                .onErrorResume(e ->
                        Mono.just(
                                Rendering.view("register")
                                        .modelAttribute("error", e.getMessage())
                                        .build()
                        )
                );
    }
}