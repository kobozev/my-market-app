package ru.yandex.practicum.mymarket.service.impl;

import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.model.Item;
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

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Flux<Order> getAll() {
        return orderRepository.findAll()
                .flatMap(this::enrichOrder)
                .sort(Comparator.comparing(Order::getId));
    }

    @Override
    public Mono<Order> getById(long id) {
        return orderRepository.findById(id)
                .flatMap(this::enrichOrder);
    }

    @Override
    public Mono<Order> create(List<CartItemDto> cartItems) {

        if (cartItems == null || cartItems.isEmpty()) {
            return Mono.error(new IllegalArgumentException("cartItems must not be null or empty"));
        }

        List<Long> ids = cartItems.stream()
                .map(ci -> ci.item().getId())
                .toList();

        Flux<Item> itemsFlux = itemRepository.findAllById(ids);

        return itemsFlux
                .collectList()
                .flatMap(items -> {

                    if (items.size() != ids.size()) {
                        return Mono.error(new ItemNotFoundException("Один или несколько товаров не найдены"));
                    }

                    Order order = new Order();

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {

                                List<OrderItem> orderItems = cartItems.stream()
                                        .map(ci -> {
                                            OrderItem oi = new OrderItem(ci.item(), ci.quantity());
                                            oi.setOrder(savedOrder);
                                            return oi;
                                        })
                                        .toList();

                                return orderItemRepository.saveAll(orderItems)
                                        .collectList()
                                        .map(savedItems -> {
                                            savedOrder.setOrderItems(savedItems);
                                            return savedOrder;
                                        });
                            });
                });
    }

    private Mono<Order> enrichOrder(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(orderItem ->
                        itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return orderItem;
                                })
                )
                .collectList()
                .map(orderItems -> {
                    orderItems.sort(Comparator.comparing(OrderItem::getItemId));
                    order.setOrderItems(orderItems);
                    return order;
                });
    }
}