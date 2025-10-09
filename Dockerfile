# Imagen base
FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiar Maven Wrapper y darle permisos
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

# Copiar el resto del proyecto
COPY . .

# 🔧 Volvemos a dar permisos por si mvnw se sobrescribió
RUN chmod +x mvnw

# Compilar sin tests
RUN ./mvnw clean package -DskipTests

# Exponer el puerto (Render usa 10000)
EXPOSE 10000

# Ejecutar la app
ENTRYPOINT ["java", "-jar", "target/fitnorius-backend-0.0.1-SNAPSHOT.jar"]
