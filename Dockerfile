# Usa una imagen base oficial de OpenJDK 21
FROM openjdk:21-jdk

# Establece el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación al contenedor
COPY build/libs/ChatServer-1.0-SNAPSHOT.jar /app/ChatServer.jar

# Define una variable de entorno para el puerto con un valor predeterminado
ENV SERVER_PORT=8080

# Expone el puerto en el que tu servidor escuchará
EXPOSE $SERVER_PORT

# Define el comando para ejecutar tu aplicación
CMD ["java", "-jar", "ChatServer.jar"]