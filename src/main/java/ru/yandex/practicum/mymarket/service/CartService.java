package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;

    public Cart getOrCreate(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseGet(() -> cartRepository.save(new Cart()));
    }

    public void add(Long cartId, Long itemId) {
        Cart cart = getOrCreate(cartId);
        Item item = itemRepository.findById(itemId).orElseThrow();

        CartItem ci = cart.getItems().stream()
                .filter(i -> i.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (ci == null) {
            ci = new CartItem();
            ci.setCart(cart);
            ci.setItem(item);
            ci.setCount(1);
            cart.getItems().add(ci);
        } else {
            ci.setCount(ci.getCount() + 1);
        }
    }

    public void minus(Long cartId, Long itemId) {
        Cart cart = getOrCreate(cartId);

        cart.getItems().removeIf(ci -> {
            if (ci.getItem().getId().equals(itemId)) {
                ci.setCount(ci.getCount() - 1);
                return ci.getCount() <= 0;
            }
            return false;
        });
    }
}