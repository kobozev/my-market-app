package ru.yandex.practicum.mymarket.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.result.view.View;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class TestViewConfig {

    @Bean
    public ViewResolver viewResolver() {

        return (viewName, locale) ->
                Mono.just(new NoOpView());
    }

    static class NoOpView implements View {

        @Override
        public @NonNull List<MediaType> getSupportedMediaTypes() {

            return List.of(
                    MediaType.TEXT_HTML,
                    MediaType.ALL
            );
        }

        @Override
        public @NonNull Mono<Void> render(
                Map<String, ?> model,
                MediaType contentType,
                ServerWebExchange exchange
        ) {

            var response = exchange.getResponse();

            response.setStatusCode(HttpStatus.OK);

            response.getHeaders().setContentType(
                    MediaType.TEXT_HTML
            );

            byte[] bytes = "<html></html>"
                    .getBytes(StandardCharsets.UTF_8);

            var buffer = response.bufferFactory()
                    .wrap(bytes);

            return response.writeWith(
                    Mono.just(buffer)
            );
        }
    }
}