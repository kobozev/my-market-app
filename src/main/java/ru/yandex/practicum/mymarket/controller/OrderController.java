package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public String buy(@SessionAttribute Long cartId) {
        Order order = orderService.createOrder(cartId);
        return "redirect:/orders/" + order.getId() + "?newOrder=true";
    }
}