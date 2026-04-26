package ru.yandex.practicum.mymarket.dto.Request;

import ru.yandex.practicum.mymarket.constants.SortType;

public final class ItemsQueryRequestDto {
    private final String search;
    private final SortType sort;
    private final Integer pageNumber;
    private final Integer pageSize;

    public ItemsQueryRequestDto(String search, SortType sort, Integer pageNumber, Integer pageSize) {
        this.search = search != null ? search : "";
        this.sort = sort != null ? sort : SortType.NO;
        this.pageNumber = pageNumber != null ? pageNumber : 1;
        this.pageSize = pageSize != null ? pageSize : 5;
    }

    public String getSearch() {
        return search;
    }

    public SortType getSort() {
        return sort;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }
}