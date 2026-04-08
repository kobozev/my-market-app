package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository repository;

    public Page<Item> find(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        search, search, pageable
                );
    }
}
