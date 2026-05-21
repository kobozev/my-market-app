package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.config.TestcontainersConfig;
import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@DataR2dbcTest
@Testcontainers
@Import(TestcontainersConfig.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        var now = LocalDateTime.now();

        item1 = buildItem("Laptop Computer", "High performance laptop", 999.99, now);
        item2 = buildItem("Desktop Computer", "Gaming desktop", 1499.99, now);
        item3 = buildItem("Wireless Mouse", "Ergonomic mouse", 29.99, now);

        StepVerifier.create(
                itemRepository.deleteAll()
                        .thenMany(itemRepository.saveAll(List.of(item1, item2, item3)))
                        .then()
        ).verifyComplete();
    }

    private Item buildItem(String title, String desc, BigDecimal price, LocalDateTime now) {
        Item item = Item.builder()
                .title(title)
                .description(desc)
                .price(BigDecimal.valueOf(price))
                .build();
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        return item;
    }

    @Test
    void findById_shouldReturnItem_whenExists() {
        StepVerifier.create(
                        itemRepository.findAll()
                                .next()
                                .flatMap(i -> itemRepository.findById(i.getId()))
                )
                .expectNextMatches(item -> item.getTitle().equals("Laptop Computer"))
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        StepVerifier.create(itemRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllItems() {
        StepVerifier.create(itemRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void save_shouldPersistNewItem() {
        Item newItem = buildItem("Keyboard", "Mechanical keyboard", 89.99, LocalDateTime.now());

        StepVerifier.create(itemRepository.save(newItem))
                .expectNextMatches(saved ->
                        saved.getId() != null &&
                                saved.getTitle().equals("Keyboard"))
                .verifyComplete();

        StepVerifier.create(itemRepository.count())
                .expectNext(4L)
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveItem() {
        StepVerifier.create(
                        itemRepository.findAll()
                                .next()
                                .flatMap(item ->
                                        itemRepository.deleteById(item.getId())
                                                .then(itemRepository.existsById(item.getId()))
                                )
                )
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldWork() {
        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                "computer",
                                org.springframework.data.domain.PageRequest.of(0, 10)
                        )
                )
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void count_shouldReturnCorrectCount() {
        StepVerifier.create(itemRepository.count())
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        StepVerifier.create(
                        itemRepository.findAll()
                                .next()
                                .flatMap(item -> itemRepository.existsById(item.getId()))
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        StepVerifier.create(itemRepository.existsById(999L))
                .expectNext(false)
                .verifyComplete();
    }
}