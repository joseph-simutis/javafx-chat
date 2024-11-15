package io.github.josephsimutis.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.ServerSocket
import java.util.UUID

class ChatServerCommand : CliktCommand() {
    private val port by option(help = "The port to open the server on.").int().default(12345)
    val configFile by option(help = "The path to the config file.").file(
        mustExist = false,
        canBeDir = false
    ).required()
    val accounts = HashMap<String, AccountInfo>()
    val clients = HashMap<UUID, Session>()

    override fun run() {
        val serverSocket = ServerSocket(port)
        if (configFile.exists()) accounts += Json.decodeFromString<HashMap<String, AccountInfo>>(configFile.readText())
        Runtime.getRuntime().addShutdownHook(Thread {
            updateConfigFile()
            clients.forEach { (_, value) ->
                value.socket.close()
            }
            serverSocket.close()
        })
        echo("Started server on port $port!")
        while (true) {
            val uuid = UUID.randomUUID()
            clients[uuid] = Session(serverSocket.accept(), null)
            ClientHandlerThread(this, uuid).apply {
                start()
            }
            echo("Session $uuid has connected! (Connected Users: ${clients.size})")
        }
    }

    fun broadcast(message: String) {
        clients.forEach { (_, client) ->
            if (client.account != null) client.writePacket(listOf("Message", message))
        }
    }

    fun updateConfigFile() { configFile.writeText(Json.encodeToString(accounts)) }
}