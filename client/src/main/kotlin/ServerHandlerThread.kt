package io.github.josephsimutis.client

import javafx.scene.Node

class ServerHandlerThread(private val app: ChatClientApplication) : Thread() {
    override fun run() {
        while (app.currentScreen == Screen.CHAT) {
            val input = app.readPacket()
            when (input.first()) {
                "Message" -> {
                    if (app.currentScreen == Screen.CHAT) {
                        app.printToScreen(input[1])
                    } else {
                        return
                    }
                }
                "Ban" -> {
                    app.changeScreen(Screen.LOGIN)
                    app.printToScreen("You have been banned!")
                    return
                }
            }
        }
    }
}