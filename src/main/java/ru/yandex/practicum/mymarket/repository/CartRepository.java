package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {}