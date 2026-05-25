package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.config.TestcontainersConfig;
import ru.yandex.practicum.mymarket.dto.CartItemDto;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.User;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.repository.UserRepository;
import ru.yandex.practicum.mymarket.service.impl.OrderServiceImpl;

import java.math.BigDecimal;
import java.util.List;

@DataR2dbcTest
@Testcontainers
@Import({OrderServiceImpl.class, TestcontainersConfig.class})
class OrderServiceTransactionalTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    private Item testItem;

    private Long userId;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                orderItemRepository.deleteAll()
                        .then(orderRepository.deleteAll())
                        .then(itemRepository.deleteAll())
                        .then(userRepository.deleteAll())
        ).verifyComplete();

        User testUser = User.builder()
                .username("testuser")
                .password("password")
                .enabled(true)
                .build();

        testUser = userRepository.save(testUser).block();
        Assertions.assertNotNull(testUser);
        userId = testUser.getId();

        testItem = Item.builder()
                .title("Test item")
                .description("Test description")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .build();

        testItem = itemRepository.save(testItem).block();
    }

    @Test
    void create_shouldPersistData_whenNoErrors() {
        List<CartItemDto> cartItems = List.of(
                new CartItemDto(testItem, 3)
        );

        StepVerifier.create(orderService.create(cartItems, userId))
                .expectNextMatches(order ->
                        order.getId() != null &&
                                order.getUserId().equals(userId) &&
                                order.getOrderItems().size() == 1
                )
                .verifyComplete();

        StepVerifier.create(orderRepository.count())
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(orderItemRepository.count())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void create_shouldRollback_whenItemNotFound() {
        Item fakeItem = Item.builder()
                .id(999L)
                .title("Fake")
                .price(BigDecimal.ONE)
                .stockQuantity(1)
                .build();

        List<CartItemDto> cartItems = List.of(
                new CartItemDto(fakeItem, 1)
        );

        StepVerifier.create(orderService.create(cartItems, userId))
                .expectError()
                .verify();

        StepVerifier.create(orderRepository.count())
                .expectNext(0L)
                .verifyComplete();

        StepVerifier.create(orderItemRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }
}