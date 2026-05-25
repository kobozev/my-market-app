package ru.yandex.practicum.mymarket.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.User;

public interface UserService extends ReactiveUserDetailsService, ReactiveUserDetailsPasswordService {
    Mono<User> registerUser(String username, String rawPassword);

    Mono<Void> loginUser(User user, ServerWebExchange exchange);
}
