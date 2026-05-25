package ru.yandex.practicum.mymarket.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Имя пользователя обязательно")

        @Size(
                min = 3,
                max = 50,
                message = "Имя пользователя должно содержать от 3 до 50 символов"
        )

        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "Допустимы только буквы, цифры, ., _, -"
        )
        String username,

        @NotBlank(message = "Пароль обязателен")

        @Size(
                min = 8,
                max = 40,
                message = "Пароль должен содержать от 8 до 40 символов"
        )

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#@$?*!%&-]).+$",
                message = "Пароль должен содержать заглавную и строчную букву, цифру и спецсимвол"
        )
        String password

) {
}