package ru.yandex.practicum.mymarket.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.OrderProcessingService;
import ru.yandex.practicum.mymarket.service.OrderService;
import ru.yandex.practicum.payment.client.api.PaymentsApi;
import ru.yandex.practicum.payment.client.model.PaymentRequest;

import java.util.List;

@Service
public class OrderProcessingServiceImpl implements OrderProcessingService {

    private static final Logger logger =
            LoggerFactory.getLogger(OrderProcessingServiceImpl.class);

    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentsApi paymentsApi;

    public OrderProcessingServiceImpl(CartService cartService,
                                      OrderService orderService,
                                      PaymentsApi paymentsApi) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentsApi = paymentsApi;
    }

    @Override
    public Mono<Order> checkout(Long userId) {

        return Mono.zip(
                        cartService.getCartItems(userId).collectList(),
                        cartService.getCartTotal(userId)
                )
                .flatMap(tuple -> {

                    var cartItems = tuple.getT1();
                    var total = tuple.getT2();

                    List<CartItemDto> cartItemDtos = cartItems.stream()
                            .map(cartItem -> new CartItemDto(
                                    cartItem.getItem(),
                                    cartItem.getQuantity()
                            ))
                            .toList();

                    return paymentsApi.processPayment(
                                    new PaymentRequest()
                                            .userId(userId)
                                            .amount(total.doubleValue())
                            )
                            .flatMap(paymentResult ->
                                    orderService.create(cartItemDtos, userId)
                            )
                            .flatMap(order ->
                                    cartService.clear(userId)
                                            .thenReturn(order)
                            );
                })
                .doOnError(error ->
                        logger.error(
                                "Checkout failed for session {}: {}",
                                userId,
                                error.getMessage()
                        )
                );
    }
}