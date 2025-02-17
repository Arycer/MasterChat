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
    mainClass.set("me.arycer.dam.client.ChatClient") // Usa set() en Kotlin DSL
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get() // Usa get() en Kotlin DSL
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveBaseName.set("ChatClient")
    archiveClassifier.set("") // Quita el sufijo "-all" para evitar confusión
    archiveVersion.set(version as String)

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}
