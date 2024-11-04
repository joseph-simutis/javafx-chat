package io.github.josephsimutis.server

import java.net.Socket

data class Session(val socket: Socket, var username: String?) {
    private val reader = socket.getInputStream().bufferedReader()
    private val writer = socket.getOutputStream().bufferedWriter()

    fun readLine(): String = reader.readLine()

    fun writeLine(str: String) = writer.write("$str\n")
}