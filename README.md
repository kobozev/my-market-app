# 🛒 My Market App

Web-приложение "Витрина интернет-магазина", реализованное на Java 21 с использованием Spring Boot 3.5, Spring Data JPA, Thymeleaf и PostgreSQL. Проект поддерживает Docker-контейнеризацию и интеграционные тесты с Testcontainers.

---

## ⚙️ Стек технологий

- **Фреймворк**: Spring Boot 3.5.12
- **Язык**: Java 21
- **База данных**: PostgreSQL 16
- **ORM**: Spring Data JPA (Hibernate)
- **Web слой**: Spring MVC + Thymeleaf
- **Валидация**: Spring Validation
- **Мониторинг**: Spring Boot Actuator
- **Сборка проекта**: Maven
- **Контейнеризация**: Docker, Docker Compose
- **Тестирование**:
    - JUnit 5
    - Spring Boot Test
    - Spring MVC Test (MockMvc)
    - Spring Data JPA Test
    - Testcontainers (PostgreSQL 16)

---

## 📦 Сборка проекта

Собрать проект можно с помощью Maven:

```bash
mvn clean package
```

После сборки будет создан JAR-файл:

`target/my-market-app-1.0.0.jar`

## 🚀 Запуск приложения

Вариант 1: через Maven
```bash
mvn spring-boot:run
```

Вариант 2: через JAR
```bash
java -jar target/my-market-app-1.0.0.jar
```

После запуска приложение будет доступно по адресу:
```
http://localhost:8080
```

## 🧪 Запуск тестов

Проект использует Testcontainers, поэтому для интеграционных тестов требуется установленный Docker.

Проверка Docker:
```bash
docker ps
```
Запуск всех тестов:
```bash
mvn clean test
```
Запуск конкретного теста:
```bash
mvn test -Dtest=CartServiceTest
```

## 🐳Запуск через Docker
1. Сборка образа
```bash
docker build -t my-market-app .
```
2. Запуск через Docker Compose
```bash
docker compose up --build
```
3. Остановка
```bash
docker compose down
```

##   🗄️ База данных

Приложение использует PostgreSQL 16.

| Переменная окружения | Описание                             |
| --- |--------------------------------------|
| `SPRING_DATASOURCE_URL` | JDBC‑строка подключения к PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Пользователь базы данных             |
| `SPRING_DATASOURCE_PASSWORD` | Пароль пользователя базы данных      |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Стратегия генерации схемы            |
	
## 📊 Actuator (Health Check)

Проверка состояния приложения:
```bash
GET http://localhost:8080/actuator/health
```

## 🧪 Особенности тестирования
* Repository тесты используют Testcontainers PostgreSQL 16
* Service layer тестируется через Spring Boot Test
* Controller layer тестируется через MockMvc
* Контекст поднимается с профилем test
* База создаётся автоматически (ddl-auto: create-drop)