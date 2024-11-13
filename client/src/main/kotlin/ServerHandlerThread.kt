package io.github.josephsimutis.client

import javafx.scene.Node

class ServerHandlerThread(private val app: ChatClientApplication) : Thread() {
    override fun run() {
        while (app.currentScreen == Screen.CHAT) {
            val input = app.readLine()
            when (input.first()) {
                'M' -> {
                    if (app.currentScreen == Screen.CHAT) {
                        app.printToScreen(input.drop(1))
                    } else {
                        return
                    }
                }
                'B' -> {
                    app.changeScreen(Screen.LOGIN)
                    app.printToScreen("You have been banned!")
                    return
                }
            }
        }
    }
}