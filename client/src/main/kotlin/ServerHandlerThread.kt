package io.github.josephsimutis.client

import io.github.josephsimutis.common.packet.PacketHeader

class ServerHandlerThread(private val app: ChatClientApplication) : Thread() {
    override fun run() {
        while (app.currentScreen == Screen.CHAT) {
            val input = app.connection!!.readPacket()
            when (input.header) {
                PacketHeader.MESSAGE -> {
                    if (app.currentScreen == Screen.CHAT) app.printToScreen(input.data[0]) else return
                }

                PacketHeader.BAN -> {
                    app.changeScreen(Screen.LOGIN)
                    app.printToScreen("You have been banned!")
                    return
                }

                else -> {}
            }
        }
    }
}