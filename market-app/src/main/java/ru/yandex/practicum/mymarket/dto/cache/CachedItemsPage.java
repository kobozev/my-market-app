package ru.yandex.practicum.mymarket.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CachedItemsPage {

    private List<CachedItem> items;

    private long total;
}