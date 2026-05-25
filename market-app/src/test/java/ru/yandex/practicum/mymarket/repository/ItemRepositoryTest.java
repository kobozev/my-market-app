package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.yandex.practicum.mymarket.config.TestcontainersConfig;
import ru.yandex.practicum.mymarket.model.Item;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataR2dbcTest
@Testcontainers
@Import(TestcontainersConfig.class)
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {

        var now = LocalDateTime.now();

        Item item1 = buildItem(
                "Laptop Computer",
                "High performance laptop",
                BigDecimal.valueOf(999.99),
                now
        );

        Item item2 = buildItem(
                "Desktop Computer",
                "Gaming desktop",
                BigDecimal.valueOf(1499.99),
                now
        );

        Item item3 = buildItem(
                "Wireless Mouse",
                "Ergonomic mouse",
                BigDecimal.valueOf(29.99),
                now
        );

        StepVerifier.create(
                itemRepository.deleteAll()
                        .thenMany(itemRepository.saveAll(List.of(item1, item2, item3)))
                        .then()
        ).verifyComplete();
    }

    private Item buildItem(
            String title,
            String description,
            BigDecimal price,
            LocalDateTime now
    ) {

        Item item = Item.builder()
                .title(title)
                .description(description)
                .price(price)
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
                                .flatMap(saved -> itemRepository.findById(saved.getId()))
                )
                .assertNext(found ->
                        assertEquals("Laptop Computer", found.getTitle())
                )
                .verifyComplete();
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {

        StepVerifier.create(itemRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_shouldReturnAllItems() {

        StepVerifier.create(itemRepository.findAll().collectList())
                .assertNext(items -> assertEquals(3, items.size()))
                .verifyComplete();
    }

    @Test
    void save_shouldPersistNewItem() {

        Item newItem = buildItem(
                "Keyboard",
                "Mechanical keyboard",
                BigDecimal.valueOf(89.99),
                LocalDateTime.now()
        );

        StepVerifier.create(itemRepository.save(newItem))
                .assertNext(saved -> {
                    assertNotNull(saved.getId());
                    assertEquals("Keyboard", saved.getTitle());
                })
                .verifyComplete();

        StepVerifier.create(itemRepository.count())
                .assertNext(count -> assertEquals(4L, count))
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
                .assertNext(exists -> assertEquals(false, exists))
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingItems_withExactMatch() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "Laptop",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(items -> {
                    assertEquals(1, items.size());
                    assertEquals(
                            "Laptop Computer",
                            items.getFirst().getTitle()
                    );
                })
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnMatchingItems_withPartialMatch() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "computer",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(items -> {
                    assertEquals(2, items.size());

                    assertTrue(
                            items.stream()
                                    .anyMatch(item ->
                                            item.getTitle().equals("Laptop Computer"))
                    );

                    assertTrue(
                            items.stream()
                                    .anyMatch(item ->
                                            item.getTitle().equals("Desktop Computer"))
                    );
                })
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldBeCaseInsensitive() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "LAPTOP",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(items -> assertEquals(1, items.size()))
                .verifyComplete();

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "laptop",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(items -> assertEquals(1, items.size()))
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldReturnEmpty_whenNoMatch() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "Nonexistent",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldSupportPagination() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "",
                                        PageRequest.of(0, 2)
                                )
                                .collectList()
                )
                .assertNext(items -> assertEquals(2, items.size()))
                .verifyComplete();

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "",
                                        PageRequest.of(1, 2)
                                )
                                .collectList()
                )
                .assertNext(items -> assertEquals(1, items.size()))
                .verifyComplete();
    }

    @Test
    void findByTitleContainingIgnoreCase_shouldHandleEmptyString() {

        StepVerifier.create(
                        itemRepository.findByTitleContainingIgnoreCase(
                                        "",
                                        PageRequest.of(0, 10)
                                )
                                .collectList()
                )
                .assertNext(items -> assertEquals(3, items.size()))
                .verifyComplete();
    }

    @Test
    void count_shouldReturnCorrectCount() {

        StepVerifier.create(itemRepository.count())
                .assertNext(count -> assertEquals(3L, count))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {

        StepVerifier.create(
                        itemRepository.findAll()
                                .next()
                                .flatMap(item ->
                                        itemRepository.existsById(item.getId()))
                )
                .assertNext(exists -> assertEquals(true, exists))
                .verifyComplete();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {

        StepVerifier.create(itemRepository.existsById(999L))
                .assertNext(exists -> assertEquals(false, exists))
                .verifyComplete();
    }
}