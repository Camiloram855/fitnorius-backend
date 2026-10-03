# ---- Etapa 1: construir el JAR ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Las pruebas usan H2 y no necesitan una base de datos externa.
RUN mvn clean package -B

# ---- Etapa 2: imagen final sin herramientas de compilación ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S -G app app \
    && mkdir -p /app/uploads \
    && chown -R app:app /app

COPY --from=builder /app/target/*.jar app.jar
RUN chown app:app /app/app.jar

USER app
EXPOSE 10000

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
