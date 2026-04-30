package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item testItem1;
    private Item testItem2;
    private List<CartItemDto> cartItemDtos;

    @BeforeEach
    void setUp() {
        testItem1 = Item.builder().id(1L).title("Item 1").price(BigDecimal.TEN).build();
        testItem2 = Item.builder().id(2L).title("Item 2").price(BigDecimal.valueOf(20)).build();

        cartItemDtos = List.of(
                new CartItemDto(testItem1, 2),
                new CartItemDto(testItem2, 3)
        );
    }

    @Test
    void getAll_shouldReturnAllOrders() {
        Order o1 = new Order(); o1.setId(1L);
        Order o2 = new Order(); o2.setId(2L);

        when(orderRepository.findAll()).thenReturn(Flux.just(o1, o2));
        when(orderItemRepository.findByOrderId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void getById_shouldReturnEmpty_whenNotExists() {
        when(orderRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getById(999L))
                .verifyComplete();
    }

    @Test
    void getById_shouldReturnOrder() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getById(1L))
                .assertNext(o -> assertEquals(1L, o.getId()))
                .verifyComplete();
    }

    @Test
    void create_shouldCreateOrderWithItems() {
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(itemRepository.findAllById(anyIterable()))
                .thenReturn(Flux.fromIterable(List.of(testItem1, testItem2)));

        when(orderRepository.save(any())).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(anyList()))
                .thenReturn(Flux.fromIterable(List.of(
                        new OrderItem(testItem1, 2),
                        new OrderItem(testItem2, 3)
                )));

        StepVerifier.create(orderService.create(cartItemDtos))
                .assertNext(order -> {
                    assertEquals(1L, order.getId());
                    assertEquals(2, order.getOrderItems().size());
                })
                .verifyComplete();
    }

    @Test
    void create_shouldRejectNull() {
        StepVerifier.create(orderService.create(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void create_shouldRejectEmpty() {
        StepVerifier.create(orderService.create(List.of()))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}