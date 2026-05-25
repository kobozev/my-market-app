package ru.yandex.practicum.mymarket.controller.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ServerWebExchange;
import ru.yandex.practicum.mymarket.dto.response.ErrorResponseDto;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.exception.OrderNotFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 (товар не найден)
    @ExceptionHandler(ItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            ItemNotFoundException ex,
            ServerWebExchange exchange) {

        log.warn("Item not found: {}", ex.getMessage());

        return build(
                exchange,
                HttpStatus.NOT_FOUND,
                "ITEM_NOT_FOUND",
                "Товар не найден"
        );
    }

    // 404 (заказ не найден)
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(
            OrderNotFoundException ex,
            ServerWebExchange exchange) {

        log.warn("Order not found: {}", ex.getMessage());

        return build(
                exchange,
                HttpStatus.NOT_FOUND,
                "ORDER_NOT_FOUND",
                "Заказ не найден"
        );
    }

    // 400 (валидация)
    @ExceptionHandler({
            BindException.class,
            WebExchangeBindException.class
    })
    public ResponseEntity<ErrorResponseDto> handleValidation(
            Exception ex,
            ServerWebExchange exchange) {

        log.warn("Validation error: {}", ex.getMessage());

        BindingResult bindingResult = switch (ex) {
            case BindException bindException ->
                    bindException.getBindingResult();

            case WebExchangeBindException webExchangeBindException ->
                    webExchangeBindException.getBindingResult();

            default -> null;
        };

        String message = bindingResult == null
                ? "Ошибка валидации"
                : bindingResult.getAllErrors()
                  .stream()
                  .findFirst()
                  .map(DefaultMessageSourceResolvable::getDefaultMessage)
                  .orElse("Ошибка валидации");

        return build(
                exchange,
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                message
        );
    }

    // 400 (некорректные параметры запроса)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(
            IllegalArgumentException ex,
            ServerWebExchange exchange) {

        log.warn("Bad request: {}", ex.getMessage());

        return build(
                exchange,
                HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Некорректный запрос"
        );
    }

    // 500
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(
            Throwable ex,
            ServerWebExchange exchange) {

        log.error("Unexpected error", ex);

        return build(
                exchange,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Произошла непредвиденная ошибка"
        );
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotAllowed(
            MethodNotAllowedException ex,
            ServerWebExchange exchange) {

        log.warn("Method not allowed: {}", ex.getMessage());

        return build(
                exchange,
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                "Метод запроса не поддерживается"
        );
    }

    private ResponseEntity<ErrorResponseDto> build(
            ServerWebExchange exchange,
            HttpStatus status,
            String code,
            String message) {

        ErrorResponseDto body = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.name())
                .code(code)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();

        return ResponseEntity.status(status).body(body);
    }
}