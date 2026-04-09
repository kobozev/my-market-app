package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OrderRepository repository;

    private Order order;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        OrderItem item = new OrderItem();
        item.setItemId(1L);
        item.setTitle("Laptop");
        item.setPrice(1000L);
        item.setCount(2);

        order = new Order();
        order.setItems(new ArrayList<>(List.of(item)));
        order.setTotalSum(2000L);

        order = repository.save(order);
    }

    @Test
    void shouldSaveOrder() {
        Order found = repository.findById(order.getId()).orElseThrow();

        assertThat(found.getItems()).hasSize(1);
    }

    @Test
    void shouldDeleteOrder() {
        repository.delete(order);

        assertThat(repository.findAll()).isEmpty();
    }
}