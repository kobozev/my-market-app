package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public Order createOrder(Long cartId) {
        List<CartItem> cartItems = cartService.getItems(cartId);

        Order order = new Order();

        List<OrderItem> orderItems = cartItems.stream().map(ci -> {
            OrderItem oi = new OrderItem();
            oi.setItemId(ci.getItem().getId());
            oi.setTitle(ci.getItem().getTitle());
            oi.setPrice(ci.getItem().getPrice());
            oi.setCount(ci.getCount());
            return oi;
        }).toList();

        order.setItems(orderItems);

        long total = orderItems.stream()
                .mapToLong(i -> i.getPrice() * i.getCount())
                .sum();

        order.setTotalSum(total);

        Order saved = orderRepository.save(order);

        cartService.clear(cartId);

        return saved;
    }
}