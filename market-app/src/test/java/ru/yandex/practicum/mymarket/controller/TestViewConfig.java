package ru.yandex.practicum.mymarket.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@TestConfiguration
public class TestViewConfig {

    @Bean
    public ViewResolver viewResolver() {
        return (viewName, locale) -> Mono.just(new NoOpView());
    }

    static class NoOpView implements View {

        @Override
        public java.util.List<MediaType> getSupportedMediaTypes() {
            return java.util.List.of(MediaType.TEXT_HTML);
        }

        @Override
        public Mono<Void> render(
                Map<String, ?> model,
                MediaType contentType,
                ServerWebExchange exchange) {

            var response = exchange.getResponse();

            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().setContentType(MediaType.TEXT_HTML);

            var buffer = response.bufferFactory()
                    .wrap("<html></html>".getBytes());

            return response.writeWith(Mono.just(buffer));
        }
    }
}