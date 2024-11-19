package io.github.josephsimutis.server

import java.io.PrintWriter
import java.net.Socket

data class Session(val socket: Socket, var account: Pair<String, AccountInfo>?) {
    private val reader = socket.getInputStream().bufferedReader()
    private val writer = PrintWriter(socket.getOutputStream(), true)

    fun readPacket(): List<String> {
        val sections = ArrayList<String>()
        repeat(reader.readLine().toInt()) { sections += reader.readLine() }
        return sections.toList()
    }

    fun writePacket(packet: List<String>) {
        writer.println(packet.size)
        for (element in packet) writer.println(element)
    }
}