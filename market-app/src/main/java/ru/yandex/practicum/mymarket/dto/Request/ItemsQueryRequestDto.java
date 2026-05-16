package ru.yandex.practicum.mymarket.dto.Request;

import ru.yandex.practicum.mymarket.constants.SortType;

public class ItemsQueryRequestDto {

    private String search;
    private SortType sort;
    private Integer pageNumber;
    private Integer pageSize;

    public ItemsQueryRequestDto() {
        this.search = "";
        this.sort = SortType.NO;
        this.pageNumber = 1;
        this.pageSize = 5;
    }

    public ItemsQueryRequestDto(String search, SortType sort, Integer pageNumber, Integer pageSize) {
        this.search = search != null ? search : "";
        this.sort = sort != null ? sort : SortType.NO;
        this.pageNumber = pageNumber != null ? pageNumber : 1;
        this.pageSize = pageSize != null ? pageSize : 5;
    }

    public String getSearch() {
        return search != null ? search : "";
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public SortType getSort() {
        return sort != null ? sort : SortType.NO;
    }

    public void setSort(SortType sort) {
        this.sort = sort;
    }

    public int getPageNumber() {
        return pageNumber != null ? pageNumber : 1;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize != null ? pageSize : 5;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}