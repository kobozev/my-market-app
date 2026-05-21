package ru.yandex.practicum.payment.repository;

import ru.yandex.practicum.payment.model.Balance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PaymentRepository extends ReactiveCrudRepository<Balance, Long> {

}
