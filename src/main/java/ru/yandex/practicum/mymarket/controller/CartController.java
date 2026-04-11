package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.mymarket.constants.CartAction;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/items")
    public String cart(@SessionAttribute(required = false) Long cartId, Model model) {
        if (cartId == null) cartId = 1L;

        CartDto cart = cartService.getCart(cartId);

        model.addAttribute("items", cart.items());
        model.addAttribute("total", cart.total());

        return "cart";
    }

    @PostMapping("/items")
    public String update(
            @SessionAttribute Long cartId,
            @RequestParam Long id,
            @RequestParam CartAction action
    ) {
        switch (action) {
            case CartAction.PLUS -> cartService.add(cartId, id);
            case CartAction.MINUS -> cartService.minus(cartId, id);
            case CartAction.DELETE -> cartService.delete(cartId, id);
        }

        return "redirect:/cart/items";
    }
}
