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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private List<CartItemDto> CartItemDtos;

    @BeforeEach
    void setUp() {
        testItem1 = Item.builder().id(1L).title("Item 1").description("Description 1").price(BigDecimal.valueOf(10.0)).build();
        testItem2 = Item.builder().id(2L).title("Item 2").description("Description 2").price(BigDecimal.valueOf(20.0)).build();

        CartItemDtos = List.of(
                new CartItemDto(testItem1, 2),
                new CartItemDto(testItem2, 3)
        );
    }

    @Test
    void getAll_shouldReturnAllOrders() {
        Order order1 = new Order();
        order1.setId(1L);
        Order order2 = new Order();
        order2.setId(2L);

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.empty());
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getAll())
                .expectNextCount(2)
                .verifyComplete();

        verify(orderRepository).findAll();
    }

    @Test
    void getAll_shouldReturnOrdersWithItems() {
        Order order = new Order();
        order.setId(1L);

        OrderItem orderItem = new OrderItem(testItem1, 2);
        orderItem.setId(1L);

        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem1));

        StepVerifier.create(orderService.getAll())
                .assertNext(o -> {
                    assertEquals(1L, o.getId());
                    assertNotNull(o.getOrderItems());
                    assertEquals(1, o.getOrderItems().size());
                })
                .verifyComplete();
    }

    @Test
    void getById_shouldReturnOrder_whenExists() {
        Order order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getById(1L))
                .assertNext(o -> assertEquals(1L, o.getId()))
                .verifyComplete();

        verify(orderRepository).findById(1L);
    }

    @Test
    void getById_shouldReturnEmpty_whenNotExists() {
        when(orderRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getById(999L))
                .verifyComplete();

        verify(orderRepository).findById(999L);
    }

    @Test
    void getById_shouldReturnOrderWithItems() {
        Order order = new Order();
        order.setId(1L);

        OrderItem orderItem = new OrderItem(testItem1, 2);
        orderItem.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem1));

        StepVerifier.create(orderService.getById(1L))
                .assertNext(o -> {
                    assertEquals(1L, o.getId());
                    assertNotNull(o.getOrderItems());
                    assertEquals(1, o.getOrderItems().size());
                    assertEquals(testItem1, o.getOrderItems().getFirst().getItem());
                })
                .verifyComplete();
    }

    @Test
    void create_shouldCreateOrderWithItems() {
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(
                List.of(
                        new OrderItem(testItem1, 2),
                        new OrderItem(testItem2, 3)
                )
        ));

        StepVerifier.create(orderService.create(CartItemDtos))
                .assertNext(order -> {
                    assertNotNull(order);
                    assertEquals(1L, order.getId());
                    assertNotNull(order.getOrderItems());
                    assertEquals(2, order.getOrderItems().size());
                })
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
        verify(orderItemRepository).saveAll(anyList());
    }

    @Test
    void create_shouldRejectNullCartItemDtos() {
        StepVerifier.create(orderService.create(null))
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectEmptyCartItemDtos() {
        StepVerifier.create(orderService.create(List.of()))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("must not be null or empty"))
                .verify();

        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_shouldSetCorrectQuantities() {
        Order savedOrder = new Order();
        savedOrder.setId(1L);

        OrderItem savedItem1 = new OrderItem(testItem1, 2);
        OrderItem savedItem2 = new OrderItem(testItem2, 3);

        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(savedItem1, savedItem2));

        StepVerifier.create(orderService.create(CartItemDtos))
                .assertNext(order -> {
                    List<OrderItem> items = order.getOrderItems();
                    assertTrue(items.stream().anyMatch(oi -> oi.getQuantity() == 2));
                    assertTrue(items.stream().anyMatch(oi -> oi.getQuantity() == 3));
                })
                .verifyComplete();
    }
}