package io.github.josephsimutis.server

import java.net.SocketTimeoutException

class ClientHandlerThread(val command: ChatServerCommand, val clientIndex: Int) : Thread() {
    override fun run() {
        try {
        } catch (e: SocketTimeoutException) {
            command.clients[clientIndex].close()
        }
    }
}