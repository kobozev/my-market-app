package ru.yandex.practicum.mymarket.controller.advice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.SecurityUser;

@ControllerAdvice
public class SecurityModelAdvice {

    @ModelAttribute("principal")
    public Mono<SecurityUser> addPrincipal() {

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(auth ->
                        auth != null
                                && auth.isAuthenticated()
                                && !(auth instanceof AnonymousAuthenticationToken)
                                && auth.getPrincipal() instanceof SecurityUser
                )
                .map(auth -> (SecurityUser) auth.getPrincipal());
    }
}