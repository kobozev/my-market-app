package ru.yandex.practicum.mymarket.service;

import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    private static final String CART_SESSION_KEY = "SHOPPING_CART";

    @Mock
    private ItemService itemService;

    @Mock
    private WebSession session;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartDto CartDto;
    private Item testItem1;
    private Item testItem2;
    private Map<String, Object> sessionAttributes;

    @BeforeEach
    void setUp() {
        CartDto = new CartDto();
        sessionAttributes = new HashMap<>();

        testItem1 = Item.builder().id(1L).title("Item 1").price(BigDecimal.valueOf(10.0)).build();
        testItem2 = Item.builder().id(2L).title("Item 2").price(BigDecimal.valueOf(20.0)).build();
    }

    @Test
    void getCart_shouldCreateNewCart_whenCartDoesNotExist() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(null);
        when(session.getAttributes()).thenReturn(sessionAttributes);

        StepVerifier.create(cartService.getCart(session))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertTrue(cart.isEmpty());
                })
                .verifyComplete();

        assertTrue(sessionAttributes.containsKey(CART_SESSION_KEY));
    }

    @Test
    void getCart_shouldReturnExistingCart_whenCartExists() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.getCart(session))
                .assertNext(cart -> assertSame(CartDto, cart))
                .verifyComplete();

        verify(session, never()).getAttributes();
    }

    @Test
    void updateItemCount_shouldAddItem_whenActionIsPlus() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.updateItemCount(session, 1L, CartAction.PLUS))
                .verifyComplete();

        assertEquals(1, CartDto.getItemCountById(1L));
    }

    @Test
    void updateItemCount_shouldIncreaseQuantity_whenItemAlreadyExists() {
        CartDto.addItem(1L, 3);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.updateItemCount(session, 1L, CartAction.PLUS))
                .verifyComplete();

        assertEquals(4, CartDto.getItemCountById(1L));
    }

    @Test
    void updateItemCount_shouldDecreaseQuantity_whenActionIsMinus() {
        CartDto.addItem(1L, 5);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.updateItemCount(session, 1L, CartAction.MINUS))
                .verifyComplete();

        assertEquals(4, CartDto.getItemCountById(1L));
    }

    @Test
    void updateItemCount_shouldRemoveItem_whenQuantityBecomesZero() {
        CartDto.addItem(1L, 1);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.updateItemCount(session, 1L, CartAction.MINUS))
                .verifyComplete();

        assertEquals(0, CartDto.getItemCountById(1L));
    }

    @Test
    void updateItemCount_shouldRemoveItem_whenActionIsDelete() {
        CartDto.addItem(1L, 5);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.updateItemCount(session, 1L, CartAction.DELETE))
                .verifyComplete();

        assertEquals(0, CartDto.getItemCountById(1L));
        assertTrue(CartDto.isEmpty());
    }

    @Test
    void removeItem_shouldRemoveItemFromCart() {
        CartDto.addItem(1L, 3);
        CartDto.addItem(2L, 2);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.removeItem(session, 1L))
                .verifyComplete();

        assertEquals(0, CartDto.getItemCountById(1L));
        assertEquals(2, CartDto.getItemCountById(2L));
    }

    @Test
    void getCartItems_shouldReturnEmptyFlux_whenCartIsEmpty() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.getCartItems(session))
                .verifyComplete();
    }

    @Test
    void getCartItems_shouldReturnItemsWithQuantities() {
        CartDto.addItem(1L, 2);
        CartDto.addItem(2L, 3);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem1));
        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));

        StepVerifier.create(cartService.getCartItems(session).collectList())
                .assertNext(items -> {
                    assertEquals(2, items.size());
                    assertTrue(items.stream().anyMatch(ci -> ci.item().getId() == 1L && ci.quantity() == 2));
                    assertTrue(items.stream().anyMatch(ci -> ci.item().getId() == 2L && ci.quantity() == 3));
                })
                .verifyComplete();
    }

    @Test
    void getCartTotal_shouldCalculateCorrectTotal() {
        CartDto.addItem(1L, 2);  // 2 * 10.0 = 20.0
        CartDto.addItem(2L, 3);  // 3 * 20.0 = 60.0
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);
        when(itemService.getById(1L)).thenReturn(Mono.just(testItem1));
        when(itemService.getById(2L)).thenReturn(Mono.just(testItem2));

        StepVerifier.create(cartService.getCartTotal(session))
                .assertNext(total -> assertEquals(80.0, total.doubleValue(), 0.01))
                .verifyComplete();
    }

    @Test
    void getCartTotal_shouldReturnZero_whenCartIsEmpty() {
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.getCartTotal(session))
                .assertNext(total -> assertEquals(BigDecimal.ZERO, total))
                .verifyComplete();
    }

    @Test
    void clear_shouldEmptyTheCart() {
        CartDto.addItem(1L, 3);
        CartDto.addItem(2L, 2);
        when(session.getAttribute(CART_SESSION_KEY)).thenReturn(CartDto);

        StepVerifier.create(cartService.clear(session))
                .verifyComplete();

        assertTrue(CartDto.isEmpty());
    }
}