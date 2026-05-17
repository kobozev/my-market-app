package ru.yandex.practicum.mymarket.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventLogger {

    private static final Logger log =
            LoggerFactory.getLogger(AuthenticationEventLogger.class);

    @EventListener
    public void onAuthenticationFailure(
            AbstractAuthenticationFailureEvent event
    ) {

        log.warn(
                "Authentication failure for user '{}' : {}",
                event.getAuthentication().getName(),
                event.getException().getMessage()
        );
    }
}