# Imagen base con Java 17
FROM eclipse-temurin:17-jdk

# Copiamos el proyecto
WORKDIR /app
COPY . .

# Compilamos con Maven
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Exponemos el puerto que usará la app
EXPOSE 8080

# Ejecutamos el jar
CMD ["java", "-jar", "target/fitnorius-backend-0.0.1-SNAPSHOT.jar"]
