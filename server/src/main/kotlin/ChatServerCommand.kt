package io.github.josephsimutis.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.ServerSocket
import java.util.UUID

class ChatServerCommand : CliktCommand() {
    private val port by argument(help = "The port to open the server on.").int()
    val configFile by argument(help = "The path to the config file.").file(
        mustExist = false,
        canBeDir = false
    )
    val accounts = HashMap<String, AccountInfo>()
    val clients = HashMap<UUID, Session>()

    override fun run() {
        echo("Starting server on port $port...")
        val serverSocket = ServerSocket(port)
        if (configFile.exists()) {
            accounts += Json.decodeFromString<HashMap<String, AccountInfo>>(configFile.readText())
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            configFile.writeText(Json.encodeToString(accounts))
            clients.forEach { (_, value) ->
                value.socket.close()
            }
            serverSocket.close()
        })
        echo("Server started!")
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
            if (client.account != null) client.writeLine("M$message")
        }
    }
}