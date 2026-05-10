package ru.yandex.practicum.payment.model;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table("balance")
public class Balance implements Persistable<UUID> {
    @Id
    @Column("user_id")
    private UUID userId;
    private BigDecimal balance;

    @Transient
    private boolean isNew =false;

    public Balance() {}

    public Balance(UUID userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
        this.isNew = true;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public @Nullable UUID getId() {
        return userId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
