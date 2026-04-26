package ru.yandex.practicum.mymarket.service.impl;

import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Flux<Order> getAll() {
        return orderRepository.findAll().flatMap(order -> orderItemRepository.findByOrderId(order.getId()).flatMap(orderItem -> itemRepository.findById(orderItem.getItemId()).map(item -> {
            orderItem.setItem(item);
            return orderItem;
        })).collectList().map(orderItems -> {
            orderItems.sort(Comparator.comparing(OrderItem::getItemId));
            order.setOrderItems(orderItems);
            return order;
        })).sort(Comparator.comparing(Order::getId));
    }

    @Override
    public Mono<Order> getById(long id) {
        return orderRepository.findById(id).flatMap(order -> orderItemRepository.findByOrderId(order.getId()).flatMap(orderItem -> itemRepository.findById(orderItem.getItemId()).map(item -> {
            orderItem.setItem(item);
            return orderItem;
        })).collectList().map(orderItems -> {
            order.setOrderItems(orderItems);
            return order;
        }));
    }

    @Override
    public Mono<Order> create(List<CartItemDto> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return Mono.error(new IllegalArgumentException("cartItems must not be null or empty"));
        }

        var order = new Order();

        return orderRepository.save(order).flatMap(savedOrder -> {
            var orderItems = cartItems.stream().map(cartItem -> {
                var orderItem = new OrderItem(cartItem.item(), cartItem.quantity());
                orderItem.setOrder(order);
                return orderItem;
            }).toList();
            return orderItemRepository.saveAll(orderItems).collectList().map(savedItems -> {
                savedOrder.setOrderItems(savedItems);
                return savedOrder;
            });
        });
    }
}
