package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.dto.ItemDto;
import ru.yandex.practicum.mymarket.mapper.ItemMapper;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper mapper;

    // Получить или создать корзину
    public Cart getOrCreate(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseGet(() -> cartRepository.save(new Cart()));
    }

    // Получить корзину как DTO
    public CartDto getCart(Long cartId) {
        Cart cart = getOrCreate(cartId);

        List<ItemDto> items = cart.getItems().stream()
                .map(ci -> mapper.toDto(ci.getItem(), ci.getCount()))
                .toList();

        long total = items.stream()
                .mapToLong(i -> i.price() * i.count())
                .sum();

        return new CartDto(items, total);
    }

    // Добавить товар
    public void add(Long cartId, Long itemId) {
        Cart cart = getOrCreate(cartId);
        Item item = itemRepository.findById(itemId).orElseThrow();

        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setItem(item);
            cartItem.setCount(1);
            cart.getItems().add(cartItem);
        } else {
            cartItem.setCount(cartItem.getCount() + 1);
        }
    }

    // Уменьшить количество
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

    // Полное удаление товара
    public void delete(Long cartId, Long itemId) {
        Cart cart = getOrCreate(cartId);
        cart.getItems().removeIf(ci -> ci.getItem().getId().equals(itemId));
    }

    // Очистка корзины (для заказа)
    public void clear(Long cartId) {
        getOrCreate(cartId).getItems().clear();
    }
}