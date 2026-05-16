FROM eclipse-temurin:21-alpine AS build
WORKDIR /app

# Copy parent and module POMs
COPY pom.xml .
COPY payment-api/pom.xml payment-api/
COPY payment-service/pom.xml payment-service/
COPY market-app/pom.xml market-app/

# Copy Maven wrapper
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -pl payment-service -am -B

# Copy sources
COPY payment-api/src payment-api/src
COPY payment-service/src payment-service/src
COPY market-app/src market-app/src

# Build application
RUN ./mvnw -pl payment-service -am clean package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# wget for healthcheck
RUN apk add --no-cache wget

# Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar
COPY --from=build /app/payment-service/target/*.jar payment-service.jar

EXPOSE 8081

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "payment-service.jar"]