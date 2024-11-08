package io.github.josephsimutis.client

import javafx.scene.Node

class ServerHandlerThread(private val app: ChatClientApplication) : Thread() {
    override fun run() {
        while (app.currentScreen == Screen.CHAT) {
            val message = app.readLine()!!.drop(1)
            if (app.currentScreen != Screen.CHAT) break
            println(message)
        }
    }
}