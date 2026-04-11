# Этап 1: Сборка зависимостей
FROM maven:3.9.6-eclipse-temurin-21 AS dependencies

WORKDIR /app

# Копируем только pom.xml для кэширования зависимостей
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Этап 2: Сборка приложения
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Кэш Maven зависимостей
COPY --from=dependencies /root/.m2 /root/.m2

# Исходники
COPY pom.xml .
COPY src ./src

# Сборка приложения
RUN mvn clean package -DskipTests

# Этап 3: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Без root пользователя
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# jar
COPY --from=builder /app/target/*.jar app.jar

# статические ресурсы
COPY --from=builder /app/src/main/resources/static /app/static

EXPOSE 8080

# Health Check
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]