# Usa una imagen base oficial de OpenJDK 21
FROM openjdk:21-jdk

# Establece el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación al contenedor
COPY build/libs/ChatServer-1.0-SNAPSHOT.jar /app/ChatServer.jar

# Expone el puerto en el que tu servidor escuchará (por ejemplo, 8080)
EXPOSE 8080

# Define el comando para ejecutar tu aplicación
CMD ["java", "-jar", "ChatServer.jar"]