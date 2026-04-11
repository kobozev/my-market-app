package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.CartDto;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Test
    void shouldReturnCartPage() throws Exception {
        when(cartService.getCart(1L))
                .thenReturn(new CartDto(List.of(), 0L));

        mockMvc.perform(get("/cart/items").sessionAttr("cartId", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));
    }

    @Test
    void shouldAddItem() throws Exception {
        mockMvc.perform(post("/cart/items")
                        .sessionAttr("cartId", 1L)
                        .param("id", "10")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection());

        verify(cartService).add(1L, 10L);
    }
}