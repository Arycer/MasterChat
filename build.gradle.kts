import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1" // Agrega Shadow Plugin
}

group = "me.arycer.dam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    // Configura la clase principal para el cliente
    mainClass.set("me.arycer.dam.client.ChatClient")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
}

// Tarea para generar el JAR del cliente con dependencias empaquetadas
task<ShadowJar>("shadowClient") {
    archiveBaseName.set("ChatClient")
    archiveClassifier.set("") // No clasificadores
    archiveVersion.set(version as String)

    from(sourceSets.main.get().output) // Incluye las clases del cliente
    from(project.configurations.getByName("runtimeClasspath")) // Incluye las dependencias del cliente

    manifest {
        attributes["Main-Class"] = application.mainClass.get() // Clase principal del cliente
    }
}

// Tarea para generar el JAR del servidor con dependencias empaquetadas
task<ShadowJar>("shadowServer") {
    archiveBaseName.set("ChatServer")
    archiveClassifier.set("") // No clasificadores
    archiveVersion.set(version as String)

    from(sourceSets.main.get().output) // Incluye las clases del servidor
    from(project.configurations.getByName("runtimeClasspath")) // Incluye las dependencias del servidor

    manifest {
        attributes["Main-Class"] = "me.arycer.dam.server.ChatServer" // Clase principal del servidor
    }
}

// Tarea personalizada que depende de shadowJar para ambos
task("buildAll") {
    dependsOn("shadowClient", "shadowServer") // Dependiendo de las tareas shadow para cliente y servidor
}