plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "io.github.josephsimutis.client"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
}

application {
    mainClass = "io.github.josephsimutis.client.MainKt"
}

javafx {
    version = "17"
    modules("javafx.controls", "javafx.fxml")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "io.github.josephsimutis.client.MainKt"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}