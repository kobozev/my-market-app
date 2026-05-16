package ru.yandex.practicum.mymarket.dto;

public record PagingDto(
        int pageNumber,
        int pageSize,
        boolean hasPrevious,
        boolean hasNext
) {
}
