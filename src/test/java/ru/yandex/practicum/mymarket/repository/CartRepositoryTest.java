package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.yandex.practicum.mymarket.model.Cart;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CartRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cartRepository.deleteAll();
        itemRepository.deleteAll();

        Item item = new Item();
        item.setTitle("Item");
        item.setPrice(100L);
        item = itemRepository.save(item);

        cart = new Cart();

        CartItem ci = new CartItem();
        ci.setCart(cart);
        ci.setItem(item);
        ci.setCount(2);

        cart.setItems(new ArrayList<>(List.of(ci)));

        cart = cartRepository.save(cart);
    }

    @Test
    void shouldPersistCartWithItems() {
        Cart found = cartRepository.findById(cart.getId()).orElseThrow();

        assertThat(found.getItems()).hasSize(1);
    }

    @Test
    void shouldCascadeDelete() {
        cartRepository.delete(cart);

        List<Cart> carts = cartRepository.findAll();
        assertThat(carts).isEmpty();
    }
}