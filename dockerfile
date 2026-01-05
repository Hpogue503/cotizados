# Usar OpenJDK 17 en Alpine (ligero)
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo dentro del contenedor
RUN apt-get update && apt-get install -y maven

WORKDIR /app
COPY . /app

# Copia el .jar generado por Maven al contenedor
COPY target/cotizador-1.0.0.jar app.jar
RUN mvn clean package -DskipTests
# Variable de entorno PORT que Render asigna
ENV PORT=8080
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]