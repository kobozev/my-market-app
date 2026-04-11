package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        ItemService.class,
        ItemServiceTest.MockConfig.class
})
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository repository;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        ItemRepository itemRepository() {
            return Mockito.mock(ItemRepository.class);
        }
    }

    @Test
    void shouldReturnAllWhenSearchNull() {
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(new Item())));

        assertEquals(1, itemService.find(null, pageable).getTotalElements());
    }

    @Test
    void shouldReturnAllWhenSearchBlank() {
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(repository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(new Item())));

        assertEquals(1, itemService.find(" ", pageable).getTotalElements());
    }

    @Test
    void shouldSearchByText() {
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(repository
                        .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                                "abc", "abc", pageable))
                .thenReturn(new PageImpl<>(List.of(new Item())));

        assertEquals(1, itemService.find("abc", pageable).getTotalElements());
    }

    @Test
    void shouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);

        Mockito.when(repository.findAll(pageable))
                .thenReturn(Page.empty());

        assertTrue(itemService.find(null, pageable).isEmpty());
    }
}