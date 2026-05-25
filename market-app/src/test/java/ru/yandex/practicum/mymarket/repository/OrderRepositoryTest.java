package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.config.TestcontainersConfig;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@Testcontainers
@Import(TestcontainersConfig.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Long testUserId;

    @BeforeEach
    void setUp() {

        StepVerifier.create(
                orderRepository.deleteAll()
                        .then(userRepository.deleteAll())
        ).verifyComplete();

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEnabled(true);

        testUserId = userRepository.save(user)
                .map(User::getId)
                .block();

        var now = LocalDateTime.now();

        Order order1 = buildOrder(now);
        Order order2 = buildOrder(now);
        Order order3 = buildOrder(now);

        StepVerifier.create(
                orderRepository.saveAll(List.of(order1, order2, order3))
                        .then()
        ).verifyComplete();
    }

    private Order buildOrder(LocalDateTime now) {

        Order order = new Order();

        order.setUserId(testUserId);

        order.setCreatedAt(now);
        order.setUpdatedAt(now);

        return order;
    }

    @Test
    void save_shouldPersistOrder() {

        Order newOrder = buildOrder(LocalDateTime.now());

        StepVerifier.create(orderRepository.save(newOrder))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertNotNull(saved.getCreatedAt());
                    assertEquals(testUserId, saved.getUserId());
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnOrder_whenExists() {

        StepVerifier.create(
                        orderRepository.findAll()
                                .next()
                                .flatMap(order ->
                                        orderRepository.findById(order.getId()))
                )
                .assertNext(found -> {
                    assertNotNull(found.getId());
                    assertEquals(testUserId, found.getUserId());
                })
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        StepVerifier.create(orderRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllOrders() {

        StepVerifier.create(orderRepository.findAll().collectList())
                .assertNext(orders -> assertEquals(3, orders.size()))
                .verifyComplete();
    }

    @Test
    void findAllByUserId_shouldReturnUserOrders() {

        StepVerifier.create(
                        orderRepository.findAllByUserId(testUserId)
                                .collectList()
                )
                .assertNext(orders -> assertEquals(3, orders.size()))
                .verifyComplete();
    }

    @Test
    void count_shouldReturnCorrectCount() {

        StepVerifier.create(orderRepository.count())
                .assertNext(count -> assertEquals(3L, count))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {

        StepVerifier.create(
                        orderRepository.findAll()
                                .next()
                                .flatMap(order ->
                                        orderRepository.existsById(order.getId()))
                )
                .assertNext(exists -> assertEquals(true, exists))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {

        StepVerifier.create(orderRepository.existsById(999L))
                .assertNext(exists -> assertEquals(false, exists))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveOrder() {

        StepVerifier.create(
                        orderRepository.findAll()
                                .next()
                                .flatMap(order ->
                                        orderRepository.deleteById(order.getId())
                                                .then(orderRepository.existsById(order.getId()))
                                )
                )
                .assertNext(exists -> assertEquals(false, exists))
                .verifyComplete();
    }

    @Test
    void deleteAll_shouldRemoveAllOrders() {

        StepVerifier.create(
                        orderRepository.deleteAll()
                                .then(orderRepository.count())
                )
                .assertNext(count -> assertEquals(0L, count))
                .verifyComplete();
    }
}