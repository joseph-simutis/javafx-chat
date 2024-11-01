plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "io.github.josephsimutis.client"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass = "io.github.josephsimutis.client.MainKt"
}

javafx {
    version = "17"
    modules("javafx.controls", "javafx.fxml")
}