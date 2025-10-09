# ---- Etapa 1: construir el JAR ----
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar el pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Construir la aplicación (sin correr tests)
RUN mvn clean package -DskipTests

# ---- Etapa 2: imagen final ----
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copiar el jar generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto (Render usa 10000 por defecto)
EXPOSE 10000

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]
