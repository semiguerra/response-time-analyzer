# Usa una imagen base de OpenJDK
FROM openjdk:21-jdk

# Define el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo jar de tu proyecto al contenedor
COPY target/response-time-analyzer-0.0.1-SNAPSHOT.jar /app/response-time-analyzer.jar

# Expone el puerto donde la app Spring Boot va a correr
EXPOSE 8080

# Define el comando para ejecutar tu aplicación
ENTRYPOINT ["java", "-jar", "response-time-analyzer.jar"]
