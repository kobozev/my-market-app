package ru.yandex.practicum.mymarket.config.security;

import ru.yandex.practicum.payment.client.ApiClient;
import ru.yandex.practicum.payment.client.api.PaymentsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuth2ClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            ReactiveClientRegistrationRepository clientRegistrationRepository,
            ReactiveOAuth2AuthorizedClientService authorizedClientService
    ) {

        return new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientService
        );
    }

    @Bean
    public PaymentsApi paymentsApi(
            ReactiveOAuth2AuthorizedClientManager authorizedClientManager
    ) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                        authorizedClientManager
                );

        oauth2Filter.setDefaultClientRegistrationId("payment-client");

        WebClient webClient = WebClient.builder()
                .filter(oauth2Filter)
                .build();

        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(paymentServiceUrl);

        return new PaymentsApi(apiClient);
    }
}