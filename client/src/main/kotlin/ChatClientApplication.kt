package io.github.josephsimutis.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.net.Socket

class ChatClientApplication : Application() {
    lateinit var primaryStage : Stage
    var currentScreen = Screen.PRECONNECT
        private set
    var socket = Socket()

    override fun start(stage: Stage) {
        primaryStage = stage
        primaryStage.title = "JavaFX Chat"
        primaryStage.scene = Scene(currentScreen.draw(this), 600.0, 400.0)
        primaryStage.show()
    }

    fun changeScreen(newScreen: Screen) {
        currentScreen = newScreen
        primaryStage.scene = Scene(currentScreen.draw(this), 600.0, 400.0)
    }
}