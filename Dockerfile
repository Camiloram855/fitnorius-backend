# Usa una imagen de Java
FROM eclipse-temurin:17-jdk-alpine

# Crea carpeta de trabajo
WORKDIR /app

# Copia pom.xml y descarga dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia el código fuente
COPY src ./src

# Compila el proyecto
RUN mvn package -DskipTests

# Expone el puerto
EXPOSE 8080

# Comando para ejecutar el JAR
CMD ["java", "-jar", "target/fitnorius-backend-0.0.1-SNAPSHOT.jar"]
