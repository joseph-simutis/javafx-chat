package io.github.josephsimutis.client

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket

class ChatClientApplication : Application() {
    lateinit var primaryStage: Stage
    private var currentScreen = Screen.PRECONNECT
    var socket: Socket? = null
        set(value) {
            field = value
            reader = value?.getInputStream()?.bufferedReader()
            writer = value?.getOutputStream()?.bufferedWriter()
        }
    private var reader: BufferedReader? = null
    private var writer: BufferedWriter? = null
    private var heart = HeartbeatThread(this)

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
        writer?.write("$str\n")
        return writer != null
    }

    fun startHeart() { if (!heart.isAlive) heart.start() }
}