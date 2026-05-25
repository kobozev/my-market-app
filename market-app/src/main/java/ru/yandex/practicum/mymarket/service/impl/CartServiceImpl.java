package ru.yandex.practicum.mymarket.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.mymarket.constants.CartAction;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.service.CacheService;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import java.math.BigDecimal;

@Service
public class CartServiceImpl implements CartService {
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    private static final String CART_CACHE_PREFIX = "cart:";

    private final ItemService itemService;
    private final CacheService cacheService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;


    public CartServiceImpl(ItemService itemService,
                           CacheService cacheService,
                           CartRepository cartRepository,
                           CartItemRepository cartItemRepository) {
        this.itemService = itemService;
        this.cacheService = cacheService;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    private String cacheKey(Long userId) {
        return CART_CACHE_PREFIX + userId;
    }

    @Override
    public Mono<Cart> getCart(Long userId) {
        String key = cacheKey(userId);

        return cacheService.get(key, Cart.class)
                .doOnNext(cart -> log.debug("Cache hit for cart: {}", userId))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache miss for cart: {}", userId);
                    return getCartFromDb(userId)
                            .flatMap(cart -> cacheService
                                    .set(key, cart)
                                    .thenReturn(cart));
                }));
    }

    private Mono<Cart> getCartFromDb(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                }))
                .flatMap(cart -> cartItemRepository.findAllByCartId(cart.getId())
                        .collectList()
                        .map(items -> {
                            cart.setItems(items);
                            return cart;
                        }));
    }

    @Override
    public Mono<Void> updateItemCount(Long userId, Long itemId, CartAction action) {
        return getCartFromDb(userId)
                .flatMap(cart -> cartItemRepository.findByCartIdAndItemId(cart.getId(), itemId)
                        .flatMap(cartItem -> {
                            // Item exists in cart - update or delete
                            if (action == CartAction.DELETE) {
                                return cartItemRepository.delete(cartItem).thenReturn(true);
                            }
                            int delta = action == CartAction.PLUS ? 1 : -1;
                            int newCount = cartItem.getQuantity() + delta;

                            if (newCount <= 0) {
                                return cartItemRepository.delete(cartItem).thenReturn(true);
                            }

                            cartItem.setQuantity(newCount);
                            return cartItemRepository.save(cartItem).thenReturn(true);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            // Item not in cart - create new if action is PLUS
                            if (action == CartAction.PLUS) {
                                CartItem newItem = new CartItem();
                                newItem.setCartId(cart.getId());
                                newItem.setItemId(itemId);
                                newItem.setQuantity(1);
                                return cartItemRepository.save(newItem).thenReturn(true);
                            }
                            return Mono.just(false);
                        }))
                        .then()
                )
                .then(invalidateCache(userId));
    }

    private Mono<Void> invalidateCache(Long userId) {
        return cacheService.delete(cacheKey(userId))
                .doOnSuccess(count -> log.debug("Invalidated cache for cart: {}, deleted: {}", userId, count))
                .then();
    }

    @Override
    public Flux<CartItem> getCartItems(Long userId) {
        return getCart(userId)
                .flatMapMany(cart -> Flux.fromIterable(cart.getItems()))
                .flatMap(cartItem ->
                        itemService.getById(cartItem.getItemId())
                                .map(item -> {
                                    cartItem.setItem(item);
                                    return cartItem;
                                }));
    }

    @Override
    public Mono<BigDecimal> getCartTotal(Long userId) {
        return getCartItems(userId)
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .defaultIfEmpty(BigDecimal.ZERO);
    }

    @Override
    public Mono<Void> removeItem(Long userId, Long itemId) {
        return updateItemCount(userId, itemId, CartAction.DELETE);
    }

    @Override
    public Mono<Void> clear(Long userId) {
        return getCartFromDb(userId)
                .flatMapMany(cart -> cartItemRepository.findAllByCartId(cart.getId()))
                .flatMap(cartItemRepository::delete)
                .then(invalidateCache(userId));
    }
}