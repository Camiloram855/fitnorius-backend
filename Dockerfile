# Imagen base con Java 17
FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiamos el pom.xml y descargamos dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B

# Copiamos todo el proyecto
COPY . .

# Construimos la aplicación
RUN ./mvnw clean package -DskipTests

# Exponemos el puerto (Render usa 10000)
EXPOSE 10000

# Comando de inicio
ENTRYPOINT ["java", "-jar", "target/app.jar"]
