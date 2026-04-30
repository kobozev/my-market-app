package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.model.Order;

import java.time.LocalDateTime;
import java.util.List;

@DataR2dbcTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("mymarketdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {

        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" +
                        postgres.getHost() + ":" +
                        postgres.getFirstMappedPort() +
                        "/" + postgres.getDatabaseName()
        );

        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();

        order1 = buildOrder(now);
        order2 = buildOrder(now);
        order3 = buildOrder(now);

        StepVerifier.create(
                orderRepository.deleteAll()
                        .thenMany(orderRepository.saveAll(List.of(order1, order2, order3)))
                        .then()
        ).verifyComplete();
    }

    private Order buildOrder(LocalDateTime now) {
        Order order = new Order();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return order;
    }

    @Test
    void save_shouldPersistOrder() {
        Order newOrder = buildOrder(LocalDateTime.now());

        StepVerifier.create(orderRepository.save(newOrder))
                .expectNextMatches(saved ->
                        saved.getId() != null &&
                                saved.getCreatedAt() != null)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnOrder_whenExists() {
        StepVerifier.create(
                        orderRepository.findAll()
                                .next()
                                .flatMap(order -> orderRepository.findById(order.getId()))
                )
                .expectNextMatches(found -> found.getId() != null)
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        StepVerifier.create(orderRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllOrders() {
        StepVerifier.create(orderRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void count_shouldReturnCorrectCount() {
        StepVerifier.create(orderRepository.count())
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        StepVerifier.create(
                        orderRepository.findAll()
                                .next()
                                .flatMap(order -> orderRepository.existsById(order.getId()))
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        StepVerifier.create(orderRepository.existsById(999L))
                .expectNext(false)
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
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void deleteAll_shouldRemoveAllOrders() {
        StepVerifier.create(
                        orderRepository.deleteAll()
                                .then(orderRepository.count())
                )
                .expectNext(0L)
                .verifyComplete();
    }
}