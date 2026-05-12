package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.service.impl.CartServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    private static final String SESSION_ID = "test-session-123";
    private static final String CACHE_KEY = "cart:" + SESSION_ID;

    @Mock
    private ItemService itemService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CacheService cacheService;

    private CartServiceImpl cartService;

    private Cart testCart;
    private Item testItem1;
    private Item testItem2;
    private CartItem testCartItem1;
    private CartItem testCartItem2;

    @BeforeEach
    void setUp() {
        cartService = new CartServiceImpl(
                itemService,
                cacheService,
                cartRepository,
                cartItemRepository
        );

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setSessionId(SESSION_ID);
        testCart.setItems(new ArrayList<>());

        testItem1 = Item.builder()
                .id(1L)
                .title("Item 1")
                .price(BigDecimal.valueOf(10.0))
                .build();

        testItem2 = Item.builder()
                .id(2L)
                .title("Item 2")
                .price(BigDecimal.valueOf(20.0))
                .build();

        testCartItem1 = new CartItem();
        testCartItem1.setId(1L);
        testCartItem1.setCartId(testCart.getId());
        testCartItem1.setItemId(1L);
        testCartItem1.setQuantity(2);

        testCartItem2 = new CartItem();
        testCartItem2.setId(2L);
        testCartItem2.setCartId(testCart.getId());
        testCartItem2.setItemId(2L);
        testCartItem2.setQuantity(3);
    }

    @Test
    void getCart_shouldReturnFromCache_whenCacheHit() {
        testCart.setItems(new ArrayList<>());

        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getCart(SESSION_ID))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(SESSION_ID, cart.getSessionId());
                })
                .verifyComplete();

        verify(cartRepository, never()).findBySessionId(anyString());
    }

    @Test
    void getCart_shouldLoadFromDbAndCache_whenCacheMiss() {
        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.empty());

        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.empty());

        when(cacheService.set(eq(CACHE_KEY), any(Cart.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(cartService.getCart(SESSION_ID))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(testCart.getId(), cart.getId());
                })
                .verifyComplete();

        verify(cartRepository).findBySessionId(SESSION_ID);
        verify(cacheService).set(eq(CACHE_KEY), any(Cart.class));
    }

    @Test
    void getCart_shouldCreateNewCart_whenCartDoesNotExist() {
        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.empty());

        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.empty());

        when(cartRepository.save(any(Cart.class)))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.empty());

        when(cacheService.set(eq(CACHE_KEY), any(Cart.class)))
                .thenReturn(Mono.just(true));

        StepVerifier.create(cartService.getCart(SESSION_ID))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(SESSION_ID, cart.getSessionId());
                })
                .verifyComplete();

        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void updateItemCount_shouldIncreaseQuantity_whenActionIsPlus() {
        testCartItem1.setQuantity(3);

        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1));

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), 1L))
                .thenReturn(Mono.just(testCartItem1));

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(Mono.just(testCartItem1));

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        cartService.updateItemCount(
                                SESSION_ID,
                                1L,
                                CartAction.PLUS
                        )
                )
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
        verify(cacheService).delete(CACHE_KEY);
    }

    @Test
    void updateItemCount_shouldDecreaseQuantity_whenActionIsMinus() {
        testCartItem1.setQuantity(5);

        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1));

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), 1L))
                .thenReturn(Mono.just(testCartItem1));

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(Mono.just(testCartItem1));

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        cartService.updateItemCount(
                                SESSION_ID,
                                1L,
                                CartAction.MINUS
                        )
                )
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateItemCount_shouldDeleteItem_whenQuantityBecomesZero() {
        testCartItem1.setQuantity(1);

        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1));

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), 1L))
                .thenReturn(Mono.just(testCartItem1));

        when(cartItemRepository.delete(any(CartItem.class)))
                .thenReturn(Mono.empty());

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        cartService.updateItemCount(
                                SESSION_ID,
                                1L,
                                CartAction.MINUS
                        )
                )
                .verifyComplete();

        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    void updateItemCount_shouldDeleteItem_whenActionIsDelete() {
        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1));

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), 1L))
                .thenReturn(Mono.just(testCartItem1));

        when(cartItemRepository.delete(any(CartItem.class)))
                .thenReturn(Mono.empty());

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        cartService.updateItemCount(
                                SESSION_ID,
                                1L,
                                CartAction.DELETE
                        )
                )
                .verifyComplete();

        verify(cartItemRepository).delete(testCartItem1);
    }

    @Test
    void updateItemCount_shouldCreateNewItem_whenItemNotInCartAndActionIsPlus() {
        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.empty());

        when(cartItemRepository.findByCartIdAndItemId(testCart.getId(), 1L))
                .thenReturn(Mono.empty());

        when(cartItemRepository.save(any(CartItem.class)))
                .thenReturn(Mono.just(testCartItem1));

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(
                        cartService.updateItemCount(
                                SESSION_ID,
                                1L,
                                CartAction.PLUS
                        )
                )
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void getCartItems_shouldReturnEmptyFlux_whenCartHasNoItems() {
        testCart.setItems(new ArrayList<>());

        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getCartItems(SESSION_ID))
                .verifyComplete();
    }

    @Test
    void getCartItems_shouldReturnCartItemsWithItems() {
        testCart.setItems(new ArrayList<>());
        testCart.getItems().add(testCartItem1);
        testCart.getItems().add(testCartItem2);

        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.just(testCart));

        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));

        when(itemService.getById(2L))
                .thenReturn(Mono.just(testItem2));

        StepVerifier.create(cartService.getCartItems(SESSION_ID).collectList())
                .assertNext(items -> {
                    assertEquals(2, items.size());

                    assertTrue(items.stream().anyMatch(
                            ci -> ci.getItem().getId() == 1L
                                    && ci.getQuantity() == 2
                    ));

                    assertTrue(items.stream().anyMatch(
                            ci -> ci.getItem().getId() == 2L
                                    && ci.getQuantity() == 3
                    ));
                })
                .verifyComplete();
    }

    @Test
    void getCartTotal_shouldCalculateCorrectTotal() {
        testCart.setItems(new ArrayList<>());
        testCart.getItems().add(testCartItem1);
        testCart.getItems().add(testCartItem2);

        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.just(testCart));

        when(itemService.getById(1L))
                .thenReturn(Mono.just(testItem1));

        when(itemService.getById(2L))
                .thenReturn(Mono.just(testItem2));

        StepVerifier.create(cartService.getCartTotal(SESSION_ID))
                .assertNext(total ->
                        assertEquals(80.0, total.doubleValue(), 0.01)
                )
                .verifyComplete();
    }

    @Test
    void getCartTotal_shouldReturnZero_whenCartIsEmpty() {
        testCart.setItems(new ArrayList<>());

        when(cacheService.get(CACHE_KEY, Cart.class))
                .thenReturn(Mono.just(testCart));

        StepVerifier.create(cartService.getCartTotal(SESSION_ID))
                .assertNext(total -> assertEquals(BigDecimal.ZERO, total))
                .verifyComplete();
    }

    @Test
    void clear_shouldDeleteAllCartItems() {
        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.just(testCartItem1, testCartItem2));

        when(cartItemRepository.delete(any(CartItem.class)))
                .thenReturn(Mono.empty());

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(cartService.clear(SESSION_ID))
                .verifyComplete();

        verify(cartItemRepository, times(2))
                .delete(any(CartItem.class));

        verify(cacheService).delete(CACHE_KEY);
    }

    @Test
    void clear_shouldCompleteSuccessfully_whenCartIsEmpty() {
        when(cartRepository.findBySessionId(SESSION_ID))
                .thenReturn(Mono.just(testCart));

        when(cartItemRepository.findAllByCartId(testCart.getId()))
                .thenReturn(Flux.empty());

        when(cacheService.delete(CACHE_KEY))
                .thenReturn(Mono.just(0L));

        StepVerifier.create(cartService.clear(SESSION_ID))
                .verifyComplete();

        verify(cartItemRepository, never())
                .delete(any(CartItem.class));

        verify(cacheService).delete(CACHE_KEY);
    }
}