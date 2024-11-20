package io.github.josephsimutis.common.packet

data class Packet(val header: PacketHeader, val data: List<String>) {
    override fun toString() = data.fold("${data.size}\n$header") { str, section -> "$str\n$section" }

    fun toPrintableString() = data.fold("$header") { str, section -> "$str;$section"}
}