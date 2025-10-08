# 🚀 Imagen base con Microsoft OpenJDK 17
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# 📁 Directorio de trabajo dentro del contenedor
WORKDIR /app

# 📦 Instala Maven (necesario si no usas mvnw)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# 📦 Copia el archivo pom.xml y descarga dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 📦 Copia el resto del código fuente (incluye mvnw si existe)
COPY . .

# ✅ Da permiso de ejecución al wrapper de Maven (si existe)
RUN chmod +x mvnw || true

# 🧱 Empaqueta la aplicación (usa mvnw si existe, si no usa mvn)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# 🔥 Expone el puerto donde correrá tu app
EXPOSE 8080

# 🏁 Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "target/fitnorius-0.0.1-SNAPSHOT.jar"]
