package ru.yandex.practicum.mymarket.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            RedirectServerLogoutSuccessHandler logoutSuccessHandler
    ) {

        RedirectServerAuthenticationSuccessHandler loginSuccessHandler =
                new RedirectServerAuthenticationSuccessHandler("/items");

        return http
                .securityContextRepository(securityContextRepository())

                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchanges -> exchanges

                        // public pages
                        .pathMatchers(HttpMethod.GET, "/", "/items", "/items/{id}")
                        .permitAll()

                        // authentication pages
                        .pathMatchers("/login", "/register")
                        .permitAll()

                        // static resources
                        .pathMatchers("/images/**", "/css/**", "/js/**")
                        .permitAll()

                        // actuator
                        .pathMatchers("/actuator/health")
                        .permitAll()

                        // secured endpoints
                        .anyExchange()
                        .authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(loginSuccessHandler)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )

                .build();
    }

    @Bean
    public RedirectServerLogoutSuccessHandler logoutSuccessHandler() {
        RedirectServerLogoutSuccessHandler handler =
                new RedirectServerLogoutSuccessHandler();

        handler.setLogoutSuccessUrl(URI.create("/"));

        return handler;
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }
}