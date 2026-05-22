package ru.yandex.practicum.mymarket.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

@TestConfiguration
@EnableWebFluxSecurity
public class TestSecurityConfig {

    static final AtomicReference<SecurityContext> MOCK_CONTEXT =
            new AtomicReference<>();

    @Bean
    public SecurityWebFilterChain testSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/login", "/register", "/items/**", "/", "/error").permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(
                        new StaticSecurityContextRepository()
                )
                .build();
    }

    private static class StaticSecurityContextRepository
            implements ServerSecurityContextRepository {

        @Override
        public Mono<Void> save(ServerWebExchange exchange,
                               SecurityContext context) {

            return Mono.empty();
        }

        @Override
        public Mono<SecurityContext> load(ServerWebExchange exchange) {

            SecurityContext context = MOCK_CONTEXT.get();

            return context != null
                    ? Mono.just(context)
                    : Mono.empty();
        }
    }

    public static void setContext(SecurityContext context) {
        MOCK_CONTEXT.set(context);
    }

    public static void clearContext() {
        MOCK_CONTEXT.set(null);
    }
}