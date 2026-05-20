package ru.yandex.practicum.mymarket.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.SecurityUser;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.repository.UserRepository;
import ru.yandex.practicum.mymarket.service.UserService;
import ru.yandex.practicum.payment.client.api.PaymentsApi;
import ru.yandex.practicum.payment.client.model.CreateBalanceRequest;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log =
            LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentsApi paymentsApi;
    private final ServerSecurityContextRepository securityContextRepository;
    private final TransactionalOperator transactionalOperator;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           PaymentsApi paymentsApi,
                           ServerSecurityContextRepository securityContextRepository,
                           TransactionalOperator transactionalOperator) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.paymentsApi = paymentsApi;
        this.securityContextRepository = securityContextRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {

        return userRepository.findByUsername(username)
                .switchIfEmpty(
                        Mono.error(
                                new UsernameNotFoundException(
                                        "Пользователь не найден: " + username
                                )
                        )
                )
                .map(SecurityUser::new);
    }

    @Override
    public Mono<UserDetails> updatePassword(UserDetails user,
                                            String newPassword) {

        return userRepository.findByUsername(user.getUsername())
                .doOnNext(u ->
                        u.setPassword(
                                passwordEncoder.encode(newPassword)
                        )
                )
                .flatMap(userRepository::save)
                .map(SecurityUser::new);
    }

    @Override
    public Mono<User> registerUser(String username,
                                   String rawPassword) {

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .build();

        return userRepository.save(user)

                .onErrorMap(
                        DataIntegrityViolationException.class,
                        e -> new IllegalArgumentException(
                                "Имя пользователя уже занято"
                        )
                )

                .flatMap(saved ->
                        paymentsApi.createBalance(
                                        new CreateBalanceRequest()
                                                .userId(saved.getId())
                                )
                                .thenReturn(saved)
                )

                .onErrorMap(
                        e -> !(e instanceof IllegalArgumentException),
                        e -> {

                            log.error(
                                    "Balance creation failed for user {}",
                                    user.getUsername(),
                                    e
                            );

                            return new RuntimeException(
                                    "Ошибка регистрации: " +
                                            "не удалось создать аккаунт. " +
                                            "Попробуйте позже."
                            );
                        }
                )

                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Void> loginUser(User user,
                                ServerWebExchange exchange) {

        SecurityUser principal = new SecurityUser(user);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        var securityContext =
                new SecurityContextImpl(authentication);

        return securityContextRepository.save(
                exchange,
                securityContext
        );
    }
}