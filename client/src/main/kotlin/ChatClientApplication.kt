package io.github.josephsimutis.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class ChatClientApplication : Application() {
    override fun start(stage: Stage) {
        stage.title = "JavaFX Chat"
        stage.scene = Scene()
        stage.show()
    }
}