package io.github.josephsimutis.common.packet

enum class PacketHeader {
    ERROR,
    MESSAGE,
    LOGIN,
    LOGOUT,
    REGISTER,
    BAN;

    override fun toString() = name.lowercase().replaceFirstChar { it.uppercaseChar() }

    companion object {
        fun fromString(str: String) = valueOf(str.uppercase())
    }
}