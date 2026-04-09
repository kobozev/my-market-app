package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private ItemRepository repository;

    private Item item1;
    private Item item2;

    @BeforeEach
    void setUp() {
        repository.deleteAll(); // важно при reuse

        item1 = new Item();
        item1.setTitle("Laptop");
        item1.setDescription("Gaming laptop");
        item1.setPrice(1000L);

        item2 = new Item();
        item2.setTitle("Phone");
        item2.setDescription("Smartphone");
        item2.setPrice(500L);

        repository.saveAll(List.of(item1, item2));
    }

    @Test
    void shouldFindAll() {
        List<Item> items = repository.findAll();
        assertThat(items).hasSize(2);
    }

    @Test
    void shouldSearchIgnoreCase() {
        Page<Item> result = repository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        "LAPTOP", "LAPTOP", PageRequest.of(0, 10)
                );

        assertThat(result.getContent()).hasSize(1);
    }
}