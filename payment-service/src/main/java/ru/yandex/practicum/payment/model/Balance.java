package ru.yandex.practicum.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("balance")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Balance {
    @Id
    private Long userId;

    private BigDecimal balance;
}