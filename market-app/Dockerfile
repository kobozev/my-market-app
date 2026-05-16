FROM eclipse-temurin:21-alpine AS build
WORKDIR /app

# Copy parent and modules POMs
COPY pom.xml .
COPY payment-api/pom.xml payment-api/
COPY market-app/pom.xml market-app/
COPY payment-service/pom.xml payment-service/

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -pl market-app -am -B

# Copy sources
COPY market-app/src market-app/src
COPY payment-api/src payment-api/src
COPY payment-service/src payment-service/src

# Build application
RUN ./mvnw -pl market-app -am clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# wget for healthcheck
RUN apk add --no-cache wget

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar
COPY --from=build /app/market-app/target/*.jar market-app.jar

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "market-app.jar"]