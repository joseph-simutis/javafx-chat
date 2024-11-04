package io.github.josephsimutis.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.serialization.json.Json
import java.net.ServerSocket

class ChatServerCommand : CliktCommand() {
    val port by argument(help = "The port to open the server on.").int()
    val timeout by option(help = "The timeout for each client, in milliseconds.").int().default(10000)
    val configFile by argument(help = "The path to the config file.").file(mustExist = false, canBeDir = false, mustBeReadable = true, mustBeWritable = true)
    val accounts = HashMap<String, String>()
    val clients = ArrayList<Session>()

    override fun run() {
        echo("Starting server on port $port...")
        val serverSocket = ServerSocket(port)
        if (configFile.exists()) {
            accounts += Json.decodeFromString<HashMap<String, String>>(configFile.readText())
        } else {
            configFile.createNewFile()
        }
        echo("Server started!")
        while (true) {
            clients += Session(serverSocket.accept().apply {
                soTimeout = timeout
            }, null)
            ClientHandlerThread(this, clients.lastIndex).apply {
                start()
            }
            echo("A client has connected! (Connected Users: ${clients.size})")
        }
    }
}