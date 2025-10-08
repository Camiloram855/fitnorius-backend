# 🚀 Imagen base con Java 17 (puedes usar 21 si tu proyecto lo usa)
FROM openjdk:17-jdk-slim

# 📁 Directorio de trabajo dentro del contenedor
WORKDIR /app

# 📦 Copia el archivo pom.xml y descarga dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 📦 Copia el resto del código fuente
COPY src ./src

# 🧱 Empaqueta la aplicación
RUN mvn clean package -DskipTests

# 🔥 Expone el puerto donde correrá tu app
EXPOSE 8080

# 🏁 Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "target/fitnorius-0.0.1-SNAPSHOT.jar"]
