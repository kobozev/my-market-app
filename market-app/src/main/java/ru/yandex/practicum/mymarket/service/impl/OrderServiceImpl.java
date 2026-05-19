package ru.yandex.practicum.mymarket.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.exception.ItemNotFoundException;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.Comparator;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final TransactionalOperator transactionalOperator;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            ItemRepository itemRepository,
                            TransactionalOperator transactionalOperator) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Flux<Order> getAll(Long userId) {

        return orderRepository.findAllByUserId(userId)
                .flatMap(this::enrichOrder)
                .sort(Comparator.comparing(Order::getId));
    }

    @Override
    public Mono<Order> getById(long id, Long userId) {

        return orderRepository.findByIdAndUserId(id, userId)
                .flatMap(this::enrichOrder);
    }

    @Override
    public Mono<Order> create(List<CartItemDto> cartItems, Long userId) {

        if (cartItems == null || cartItems.isEmpty()) {
            return Mono.error(
                    new IllegalArgumentException("cartItems must not be null or empty")
            );
        }

        return Mono.defer(() -> {

                    List<Long> ids = cartItems.stream()
                            .map(ci -> ci.item().getId())
                            .toList();

                    return itemRepository.findAllById(ids)
                            .collectMap(Item::getId)
                            .flatMap(itemMap -> {

                                if (itemMap.size() != ids.size()) {
                                    return Mono.error(
                                            new ItemNotFoundException(
                                                    "Один или несколько товаров не найдены"
                                            )
                                    );
                                }

                                Order order = new Order();
                                order.setUserId(userId);

                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> {

                                            List<OrderItem> orderItems = cartItems.stream()
                                                    .map(ci -> {

                                                        Item itemFromDb =
                                                                itemMap.get(ci.item().getId());

                                                        if (itemFromDb == null) {
                                                            throw new ItemNotFoundException(
                                                                    "Item not found: "
                                                                            + ci.item().getId()
                                                            );
                                                        }

                                                        OrderItem orderItem =
                                                                new OrderItem(
                                                                        itemFromDb,
                                                                        ci.quantity()
                                                                );

                                                        orderItem.setOrder(savedOrder);

                                                        return orderItem;
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
                })
                .as(transactionalOperator::transactional);
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

                    orderItems.sort(
                            Comparator.comparing(OrderItem::getItemId)
                    );

                    order.setOrderItems(orderItems);

                    return order;
                });
    }
}