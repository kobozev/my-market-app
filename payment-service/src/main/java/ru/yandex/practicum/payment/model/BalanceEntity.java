package ru.yandex.practicum.payment.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("balance")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BalanceEntity {
    @Id
    private Long userId;

    private Double balance;
}