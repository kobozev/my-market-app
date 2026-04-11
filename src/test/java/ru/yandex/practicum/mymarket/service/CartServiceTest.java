package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
        CartService.class,
        CartServiceTest.MockConfig.class
})
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemMapper mapper;

    @TestConfiguration
    static class MockConfig {

        @Bean
        @Primary
        CartRepository cartRepository() {
            return Mockito.mock(CartRepository.class);
        }

        @Bean
        @Primary
        ItemRepository itemRepository() {
            return Mockito.mock(ItemRepository.class);
        }

        @Bean
        @Primary
        ItemMapper itemMapper() {
            return Mockito.mock(ItemMapper.class);
        }
    }

    private Cart cart;
    private Item item;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>());

        item = new Item();
        item.setId(10L);
        item.setPrice(100L);
        item.setTitle("Item");

        Mockito.when(cartRepository.findById(1L))
                .thenReturn(Optional.of(cart));

        Mockito.when(itemRepository.findById(10L))
                .thenReturn(Optional.of(item));
    }

    @Test
    void shouldCreateCartIfNotExists() {
        Mockito.when(cartRepository.findById(2L)).thenReturn(Optional.empty());
        Mockito.when(cartRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        Cart result = cartService.getOrCreate(2L);

        assertNotNull(result);
        verify(cartRepository).save(any());
    }

    @Test
    void shouldAddNewItem() {
        cartService.add(1L, 10L);

        assertEquals(1, cart.getItems().size());
        assertEquals(1, cart.getItems().getFirst().getCount());
    }

    @Test
    void shouldIncreaseCount() {
        cartService.add(1L, 10L);
        cartService.add(1L, 10L);

        assertEquals(2, cart.getItems().getFirst().getCount());
    }

    @Test
    void shouldDecreaseCount() {
        cartService.add(1L, 10L);
        cartService.add(1L, 10L);

        cartService.minus(1L, 10L);

        assertEquals(1, cart.getItems().getFirst().getCount());
    }

    @Test
    void shouldRemoveItemWhenCountZero() {
        cartService.add(1L, 10L);

        cartService.minus(1L, 10L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldDeleteItem() {
        cartService.add(1L, 10L);

        cartService.delete(1L, 10L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldIgnoreDeleteIfItemNotExists() {
        cartService.delete(1L, 999L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldClearCart() {
        cartService.add(1L, 10L);

        cartService.clear(1L);

        assertTrue(cart.getItems().isEmpty());
    }

    @Test
    void shouldCalculateTotal() {
        cartService.add(1L, 10L);
        cartService.add(1L, 10L);

        Mockito.when(mapper.toDto(any(), anyInt()))
                .thenReturn(new ItemDto(10L, "t", "d", "img", 100L, 2));

        CartDto dto = cartService.getCart(1L);

        assertEquals(200L, dto.total());
        assertEquals(1, dto.items().size());
    }

    @Test
    void shouldReturnEmptyCartDto() {
        CartDto dto = cartService.getCart(1L);

        assertEquals(0, dto.total());
        assertTrue(dto.items().isEmpty());
    }
}