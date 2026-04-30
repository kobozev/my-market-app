# My Market App

Реактивное Web-приложение интернет-магазина, разработанное на Spring Boot с использованием **Spring WebFlux** и реактивного стека данных (R2DBC).

## Описание

My Market App — учебный проект интернет-магазина, построенный на полностью реактивной архитектуре.

Приложение позволяет:
- Просматривать каталог товаров
- Добавлять товары в корзину (на основе WebSession)
- Управлять корзиной (количество, очистка)
- Оформлять заказы из корзины
- Просматривать список заказов
- Просматривать детали заказа

Архитектура построена на неблокирующем I/O и реактивных потоках (`Mono` / `Flux`).

---

## Технологии

### Backend (реактивный стек)
- Spring Boot 3.5.12
- Spring WebFlux — реактивный веб-слой
- Project Reactor — Mono / Flux
- Spring Data R2DBC — реактивный доступ к БД
- R2DBC PostgreSQL driver — неблокирующий драйвер PostgreSQL
- Thymeleaf (reactive rendering) — серверные HTML представления
- Spring Validation — валидация входных данных
- Spring Boot Actuator — мониторинг приложения

---

### База данных
- PostgreSQL 16 — основная БД
- R2DBC — реактивное подключение к базе

---

### Тестирование
- JUnit 5
- Mockito
- Spring WebTestClient — тестирование WebFlux контроллеров
- Reactor Test — тестирование реактивных цепочек
- Testcontainers
  - PostgreSQL container
  - R2DBC Testcontainers integration
- Spring Boot Test (WebFlux only)

---

### Сборка
- Maven 3.9+
- Spring Boot Maven Plugin

---

## Архитектура

Приложение использует полностью реактивный pipeline:

```
Controller (WebFlux)
        ↓ Mono / Flux
Service Layer (business logic)
        ↓ Mono / Flux
Repository Layer (R2DBC)
        ↓ Reactive Streams
PostgreSQL
```

---

## Основные компоненты

### Controllers

- ItemController — каталог товаров
- CartController — корзина (WebSession)
- OrderController — оформление и просмотр заказов

---

### Services

- ItemService — бизнес-логика товаров
- CartService — работа с корзиной в WebSession
- OrderService — создание и получение заказов

---

### Repositories

- ItemRepository — реактивный доступ к товарам
- OrderRepository — реактивный доступ к заказам
- OrderItemRepository — позиции заказа

---

## Особенности реализации

### Реактивная модель
Все операции используют:
- Mono<T> — 0..1 элемент
- Flux<T> — 0..N элементов

### Работа с корзиной
Корзина хранится в:
- WebSession
- не в базе данных

### Заказы
- создаются из корзины
- сохраняются через R2DBC
- имеют связь с OrderItem

---

## Установка и запуск

### Требования
- Java 21+
- Docker
- Maven 3.9+

---

### 1. Запуск базы данных

```bash
docker-compose -f docker/docker-compose.yml up -d
```

Запускается:
- PostgreSQL
- база данных marketdb

---

### 2. Запуск приложения

```bash
mvn spring-boot:run
```

или

```bash
mvn clean package
java -jar target/my-market-app-1.0.0.jar
```

---

### 3. Доступ к приложению

```
http://localhost:8080
```

---

## Тестирование

### Запуск всех тестов

```bash
mvn clean test
```

### Запуск конкретного теста
```bash
mvn test -Dtest=CartServiceTest
```

---

### Testcontainers

Интеграционные тесты используют:
- PostgreSQL container
- R2DBC driver

---

### WebFlux тесты

Контроллеры тестируются через:
- @WebFluxTest
- WebTestClient
- Mockito

---

## Примечания по архитектуре

- Spring MVC полностью исключён
- Используется только WebFlux stack
- JDBC отсутствует — только R2DBC
- Контроллеры возвращают Rendering (server-side views)
- Все I/O операции неблокирующие

---