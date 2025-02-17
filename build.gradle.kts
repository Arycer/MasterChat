plugins {
    id("java")
    id("application")
}

group = "me.arycer.dam"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "me.arycer.dam.client.ChatClient"
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation ("io.netty:netty-all:4.1.115.Final")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
}

tasks.test {
    useJUnitPlatform()
}