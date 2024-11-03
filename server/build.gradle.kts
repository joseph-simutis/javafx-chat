plugins {
    application
}

group = "io.github.josephsimutis.server"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
}

application {
    mainClass = "io.github.josephsimutis.server.MainKt"
}