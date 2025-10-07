# Imagen base con Java 17
FROM eclipse-temurin:17-jdk

# Directorio de trabajo
WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Compilar el backend (usa Maven wrapper si existe)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Exponer el puerto
EXPOSE 8080

# Ejecutar el .jar resultante
CMD ["java", "-jar", "target/fitnorius-backend-0.0.1-SNAPSHOT.jar"]
