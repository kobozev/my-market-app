package ru.yandex.practicum.mymarket.exception;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemNotFoundException extends RuntimeException {

    private final Set<Long> missingIds;

    public ItemNotFoundException(Long id) {
        super("Товар с id=" + id + " не найден");
        this.missingIds = Set.of(id);
    }

    public ItemNotFoundException(Set<Long> ids) {
        super(buildMessage(ids));
        this.missingIds = ids;
    }

    public ItemNotFoundException(String message) {
        super(message);
        this.missingIds = Set.of();
    }

    public Set<Long> getMissingIds() {
        return missingIds;
    }

    private static String buildMessage(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "Один или несколько товаров не найдены";
        }

        return "Не найдены товары с id: " +
                ids.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
    }
}