package io.github.josephsimutis.server

import java.io.PrintWriter
import java.net.Socket

data class Session(val socket: Socket, var username: String?) {
    private val reader = socket.getInputStream().bufferedReader()
    private val writer = PrintWriter(socket.getOutputStream(), true)

    fun readLine(): String = reader.readLine()

    fun writeLine(str: String) {
        writer.println(str)
    }
}