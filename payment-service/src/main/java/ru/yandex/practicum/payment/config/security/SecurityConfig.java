package ru.yandex.practicum.payment.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String CLIENT_ID = "payment-service";

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {

        return http

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges

                        .pathMatchers("/actuator/health")
                        .permitAll()

                        .anyExchange()
                        .hasAuthority("payment.balance.manage")
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )
                        )
                )

                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Map<String, Object> resourceAccess =
                    jwt.getClaimAsMap("resource_access");

            if (resourceAccess == null) {
                return Collections.emptyList();
            }

            Map<String, Object> clientResource =
                    (Map<String, Object>) resourceAccess.get(CLIENT_ID);

            if (clientResource == null) {
                return Collections.emptyList();
            }

            List<String> roles =
                    (List<String>) clientResource.get("roles");

            if (roles == null) {
                return Collections.emptyList();
            }

            return roles.stream()
                    .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                    .toList();
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}