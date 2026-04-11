package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        OrderService.class,
        OrderServiceTest.MockConfig.class
})
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @TestConfiguration
    static class MockConfig {

        @Bean
        @Primary
        OrderRepository orderRepository() {
            return Mockito.mock(OrderRepository.class);
        }

        @Bean
        @Primary
        CartService cartService() {
            return Mockito.mock(CartService.class);
        }
    }

    @Test
    void shouldCreateOrder() {
        Item item = new Item();
        item.setId(1L);
        item.setPrice(100L);
        item.setTitle("Item");

        CartItem ci = new CartItem();
        ci.setItem(item);
        ci.setCount(2);

        Cart cart = new Cart();
        cart.setItems(List.of(ci));

        Mockito.when(cartService.getOrCreate(1L)).thenReturn(cart);
        Mockito.when(orderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(1L);

        assertEquals(200L, order.getTotalSum());
        assertEquals(1, order.getItems().size());

        verify(cartService).clear(1L);
    }

    @Test
    void shouldCreateEmptyOrder() {
        Cart cart = new Cart();
        cart.setItems(List.of());

        Mockito.when(cartService.getOrCreate(1L)).thenReturn(cart);
        Mockito.when(orderRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Order order = orderService.createOrder(1L);

        assertEquals(0, order.getTotalSum());
        assertTrue(order.getItems().isEmpty());
    }
}