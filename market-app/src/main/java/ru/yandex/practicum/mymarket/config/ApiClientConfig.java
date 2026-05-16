package ru.yandex.practicum.mymarket.config;

import ru.yandex.practicum.payment.client.ApiClient;
import ru.yandex.practicum.payment.client.api.PaymentsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiClientConfig {

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Bean
    public PaymentsApi paymentsApi() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(paymentServiceUrl);
        return new PaymentsApi(apiClient);
    }
}
