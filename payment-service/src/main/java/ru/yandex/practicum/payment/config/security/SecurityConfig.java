package ru.yandex.practicum.payment.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges

                        // actuator health endpoint
                        .pathMatchers("/actuator/health")
                        .permitAll()

                        // secured payment endpoints
                        .anyExchange()
                        .hasAuthority("SCOPE_payments")
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt()
                )

                .build();
    }
}