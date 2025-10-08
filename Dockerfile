# Imagen base con Java 17
FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiamos archivos de Maven wrapper
COPY mvnw .
COPY .mvn .mvn

# 🔧 Dar permisos de ejecución al script mvnw
RUN chmod +x mvnw

# Descargamos dependencias para cachear
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copiamos el resto del proyecto
COPY . .

# Construimos la app (sin correr tests)
RUN ./mvnw clean package -DskipTests

# Exponemos el puerto (Render usa 10000)
EXPOSE 10000

# Comando de inicio
ENTRYPOINT ["java", "-jar", "target/app.jar"]
