# Stage 1: Build
FROM maven:3.8.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia solo pom.xml primero para cache de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente
COPY src ./src

# Compila el proyecto sin tests
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copia el .jar desde la etapa de build
COPY --from=build /app/target/cotizador-1.0.0.jar app.jar

# Variable de entorno PORT que Render asigna
ENV PORT=8080
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]