package io.github.josephsimutis.client

import io.github.josephsimutis.common.Connection
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class ChatClientApplication : Application() {
    lateinit var primaryStage: Stage
    var currentScreen = Screen.PRECONNECT
        private set
    var connection: Connection? = null

    override fun start(stage: Stage) {
        primaryStage = stage
        primaryStage.title = "JavaFX Chat"
        changeScreen(Screen.PRECONNECT)
        primaryStage.show()
    }

    fun changeScreen(newScreen: Screen) {
        currentScreen = newScreen
        primaryStage.scene = Scene(currentScreen.draw(this), newScreen.width, newScreen.height)
    }

    fun printToScreen(str: String) {
        currentScreen.printToScreen(str)
    }
}