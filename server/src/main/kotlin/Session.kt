package io.github.josephsimutis.server

import java.io.PrintWriter
import java.net.Socket

data class Session(val socket: Socket, var account: Pair<String, AccountInfo>?) {
    private val reader = socket.getInputStream().bufferedReader()
    private val writer = PrintWriter(socket.getOutputStream(), true)

    fun readPacket(): List<String> = reader.readLine().split(';')

    fun writePacket(packet: List<String>) { writer.println(packet.drop(1).fold(packet[0]) { str, section -> "$str;$section" }) }
}