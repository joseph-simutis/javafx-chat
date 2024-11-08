package io.github.josephsimutis.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.PrintWriter
import java.net.Socket

class ChatClientApplication : Application() {
    lateinit var primaryStage: Stage
    var currentScreen = Screen.PRECONNECT
        private set
    var socket: Socket? = null
        set(value) {
            field = value
            reader = value?.getInputStream()?.bufferedReader()
            writer = value?.getOutputStream()?.let { PrintWriter(it, true) }
        }
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null

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

    fun readLine(): String? = reader?.readLine()

    fun writeLine(str: String): Boolean {
        writer?.println(str)
        return writer != null
    }
}