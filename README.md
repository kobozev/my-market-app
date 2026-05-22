# My Market App

Реактивное multi-module веб-приложение интернет-магазина, разработанное на Spring Boot с использованием **Spring WebFlux**, **R2DBC**, **Redis**, **Spring Security** и интеграцией внутреннего платежного сервиса.

## Описание

My Market App - учебный проект интернет-магазина, построенный на полностью реактивной архитектуре.

Приложение позволяет:

- Регистрироваться и входить в систему
- Просматривать каталог товаров
- Выполнять поиск и сортировку товаров
- Управлять корзиной (добавление, изменение количества, удаление)
- Оформлять заказы
- Выполнять обработку платежей через внутренний платежный сервис
- Просматривать список заказов
- Просматривать детали заказов

Анонимные пользователи могут просматривать каталог товаров. 

Корзина, заказы и оплата доступны только авторизованным пользователям.

### Аутентификация и авторизация

- **Пользователь → market-app**: Form-based login (username/password хранятся в PostgreSQL) с использованием Spring Security
- **market-app → payment-service**: Service-to-service взаимодействие через OAuth2 Client Credentials Flow:
  - market-app получает access token от Keycloak (Authorization Server)
  - payment-service выступает как OAuth2 Resource Server и валидирует JWT.

Проект реализован как multi-module приложение с разделением на:

- основное приложение магазина (`market-app`)
- внутренний сервис обработки платежей (`payment-service`)
- OpenAPI контракты и generated API (`payment-api`)

Архитектура построена на реактивных потоках (`Mono` / `Flux`) и неблокирующем вводе/выводе.

---

# Структура проекта

```text
my-market-app/
├── market-app/          # Основное приложение интернет-магазина
├── payment-service/     # Внутренний сервис обработки платежей
├── payment-api/         # OpenAPI контракты и generated API
├── docker/              # Docker конфигурации
│   ├── docker-compose.yml
│   └── keycloak/        # Keycloak realm конфигурация
└── proxy/               # Настройки Nginx Reverse proxy 

```

---

# Технологии

## Backend

- Java 21
- Spring Boot 3.5.12
- Spring WebFlux
- Spring Security
- Spring OAuth2 Client - Client Credentials Flow
- Spring OAuth2 Resource Server - JWT validation in payment-service
- Project Reactor (Mono / Flux)
- Spring Validation
- Spring Boot Actuator
- Thymeleaf (server-side rendering with WebFlux support)

---

## Реактивный доступ к данным

- Spring Data R2DBC
- R2DBC PostgreSQL Driver
- Reactive repositories (R2DBC-based data access)

---

## База данных и кэширование

- PostgreSQL - основная база данных приложения
- R2DBC PostgreSQL - реактивный PostgreSQL драйвер
- Redis - персистентное хранилище корзины
- Spring Data Redis Reactive - реактивная работа с Redis
- Testcontainers PostgreSQL - контейнеризированная PostgreSQL для интеграционных тестов
---

## Межсервисное взаимодействие

- OpenAPI Generator
- payment-api module
- Reactive WebClient
- Keycloak - OAuth2 сервер для service-to-service аутентификации

---

## Тестирование

- JUnit 5
- Mockito
- Reactor Test
- WebTestClient
- Spring Boot Test
- Testcontainers
  - PostgreSQL container
  - R2DBC integration

---

## Сборка

- Maven Multi-Module
- Maven Wrapper (`./mvnw`)
- Spring Boot Maven Plugin

---

# Архитектура

Приложение использует полностью реактивный pipeline:

```text
Controller (WebFlux)
        ↓ Mono / Flux
Service Layer
        ↓ Mono / Flux
Repository Layer (R2DBC / Redis Reactive)
        ↓ Reactive Streams
PostgreSQL / Redis
```

---

# Модули проекта

## market-app

Основное веб-приложение интернет-магазина.

### Основные компоненты

#### Controllers

- `AuthController` - регистрация и вход пользователей
- `ItemController` - каталог товаров
- `CartController` - корзина
- `OrderController` - оформление и просмотр заказов

---

#### Services

- `UserService` - регистрация, аутентификация, программный логин после регистрации
- `ItemService` - получение каталога товаров, поиск, сортировка и загрузка товаров по идентификаторам
- `CartService` - управление корзиной, изменением количества товаров, очисткой корзины и расчетом общей стоимости
- `OrderService` - создание заказов и получение информации о заказах
- `OrderProcessingService` - orchestration layer оформления заказа:
  - получение содержимого корзины
  - расчет итоговой стоимости
  - выполнение платежа через `payment-service`
  - создание заказа
  - очистка корзины после успешного checkout
- `CacheService` - абстракция над Redis для реактивной работы с кэшем и персистентным хранением корзины

---

#### Repositories

- `UserRepository` - реактивный доступ к данным пользователей
- `ItemRepository` - реактивный доступ к данным товаров и операциям каталога
- `CartRepository` - работа с корзинами пользователей, сохранёнными в Redis
- `CartItemRepository` - управление позициями корзины и количеством товаров
- `OrderRepository` - реактивный доступ к данным заказов
- `OrderItemRepository` - работа с позициями заказов и составом заказа

---

## payment-service

Внутренний реактивный сервис обработки платежей.
Защищён JWT авторизацией — принимает запросы только с валидным OAuth2 токеном и ролью `payment.balance.manage`.

### Основные компоненты

#### Controllers

- `PaymentController` - API обработки платежей

---

#### Services

- `PaymentService` - логика платежей и баланса

---

#### Repositories

- `PaymentRepository`

---

## payment-api

Модуль OpenAPI контрактов.

Используется для:

- генерации DTO
- генерации API REST-контроллера сервиса платежей
- генерации HTTP-клиента для сервиса покупки основного приложения

---

## Особенности реализации

### Полностью реактивный стек

Проект полностью использует WebFlux и Reactor.

### Персистентная корзина

Корзина хранится в Redis и не зависит от `WebSession`.

Для корзины используется:

- `spring-boot-starter-data-redis-reactive`
- неблокирующий Redis driver
- reactive repositories

### Межсервисное взаимодействие

`market-app` взаимодействует с `payment-service` через:

- OpenAPI generated API
- WebClient
- Reactive HTTP calls

### Реактивные транзакции

Для заказов используются:

- R2DBC Transactional Operator
- rollback при ошибках оплаты или сохранения заказа

---

# Установка и запуск

## Требования

- Java 21+
- Docker
- Docker Compose

Maven отдельно устанавливать не требуется - используется Maven Wrapper.

---

# Запуск приложения

## Запуск всей инфраструктуры

```bash
docker compose -f docker/docker-compose.yml up --build
```

Будут запущены:

- market-app
- payment-service
- PostgreSQL
- Redis

---

# Локальная сборка

## Сборка всех модулей

```bash
./mvnw clean package
```

---

## Запуск market-app

```bash
./mvnw spring-boot:run -pl market-app
```

---

## Запуск payment-service

```bash
./mvnw spring-boot:run -pl payment-service
```

---

# Доступ к приложению

## Reverse Proxy

После запуска приложение доступно через Nginx:

```text
http://localhost
```

Nginx проксирует запрос к основному приложению market-app:

```text
http://market-app:8080
```

---

## Payment Service

```text
http://localhost:8081
```

---

# Тестирование

## Требования

Для интеграционных тестов необходим Docker.

Testcontainers автоматически поднимает PostgreSQL контейнеры во время выполнения тестов.

---

## Запуск всех тестов

```bash
./mvnw test
```

---

## Запуск тестов конкретного модуля

### market-app

```bash
./mvnw test -pl market-app
```

### payment-service

```bash
./mvnw test -pl payment-service
```

---

## Запуск конкретного теста

### market-app

```bash
./mvnw test -pl market-app -Dtest=CartServiceTest
```

### payment-service

```bash
./mvnw test -pl payment-service -Dtest=PaymentServiceTest
```

---

# WebFlux тестирование

Контроллеры тестируются через:

- `@WebFluxTest`
- `WebTestClient`
- Mockito

---

# Интеграционные тесты

Используются:

- Testcontainers
- PostgreSQL container
- Reactor StepVerifier
- R2DBC integration

---

# Мониторинг

В проекте подключен Spring Boot Actuator.

Доступные endpoints:

```text
/actuator/health
/actuator/info
```

---