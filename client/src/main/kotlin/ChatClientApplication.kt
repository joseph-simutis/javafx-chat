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
    private var connection: Triple<Socket, BufferedReader, PrintWriter>? = null
    var socket: Socket?
        get() = connection?.first
        set(value) {
            connection = value?.let {
                Triple(
                    it,
                    it.getInputStream().bufferedReader(),
                    PrintWriter(it.getOutputStream(), true)
                )
            }
        }

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

    fun readPacket(): List<String> {
        val sections = ArrayList<String>()
        repeat(connection!!.second.readLine().toInt()) { sections += connection!!.second.readLine() }
        return sections.toList()
    }

    fun writePacket(packet: List<String>) {
        connection!!.third.println(packet.size)
        for (element in packet) connection!!.third.println(element)
    }

    fun printToScreen(str: String) {
        currentScreen.printToScreen(str)
    }
}