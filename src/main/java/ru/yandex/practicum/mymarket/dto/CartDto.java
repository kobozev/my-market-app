package ru.yandex.practicum.mymarket.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class CartDto implements Serializable {
    private final Map<Long, Integer> items = new HashMap<>();

    public void addItem(long itemId, Integer quantity) {
        items.merge(itemId, quantity, Integer::sum);
    }

    public void removeItem(long itemId) {
        items.remove(itemId);
    }

    public void updateItem(long itemId, Integer quantity) {
        if (quantity <= 0) {
            removeItem(itemId);
        } else {
            items.put(itemId, quantity);
        }
    }

    public void clear() {
        items.clear();
    }

    public Map<Long, Integer> getItems() {
        return items;
    }

    public int getItemCountById(long itemId) {
        return items.getOrDefault(itemId, 0);
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}