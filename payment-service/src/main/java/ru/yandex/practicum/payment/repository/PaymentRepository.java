package ru.yandex.practicum.payment.repository;

import ru.yandex.practicum.payment.model.BalanceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PaymentRepository extends ReactiveCrudRepository<BalanceEntity, Long> {

}
