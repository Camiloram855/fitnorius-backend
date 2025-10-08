# 🧩 Etapa 1: build con Maven y JDK
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar POM primero para aprovechar caché de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# 🧩 Etapa 2: runtime (solo el JDK para ejecutar el jar)
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copiar el JAR generado desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Render usa su propia variable de entorno PORT
# Así que aseguramos que el contenedor escuche en ese puerto dinámico
ENV PORT=8080
EXPOSE 8080

# 🧠 El comando de arranque usará el puerto asignado por Render
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
