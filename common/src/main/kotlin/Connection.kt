package io.github.josephsimutis.common

import io.github.josephsimutis.common.packet.Packet
import io.github.josephsimutis.common.packet.PacketHeader
import java.net.Socket
import java.io.BufferedReader
import java.io.PrintWriter

class Connection private constructor (val socket: Socket, private val reader: BufferedReader, private val writer: PrintWriter) {
    constructor(socket: Socket) : this(
        socket,
        socket.getInputStream().bufferedReader(),
        PrintWriter(socket.getOutputStream(), true)
    )

    fun readPacket(): Packet = Triple(
        reader.readLine().toInt(),
        PacketHeader.fromString(reader.readLine()),
        ArrayList<String>()
    ).let { (lineCount, header, lines) ->
        repeat(lineCount) {
            lines += reader.readLine()
        }
        Packet(header, lines)
    }

    fun writePacket(header: PacketHeader, vararg data: String) {
        writer.println(Packet(header, data.toList()))
    }
}