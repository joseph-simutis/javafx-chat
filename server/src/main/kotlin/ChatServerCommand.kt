package io.github.josephsimutis.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import java.net.ServerSocket
import java.net.Socket

class ChatServerCommand : CliktCommand() {
    val port by argument(help = "The port to open the server on.").int()
    val timeout by option(help = "The timeout for each client, in milliseconds.").int().default(10000)
    lateinit var serverSocket: ServerSocket
    val clients = ArrayList<Socket>()

    override fun run() {
        echo("Starting server on port $port...")
        serverSocket = ServerSocket(port)
        echo("Server started!")
        while (true) {
            clients += serverSocket.accept().apply {
                soTimeout = timeout
                clients += this
            }
            ClientHandlerThread(this, clients.lastIndex).apply {
                start()
            }
            echo("A client has connected! (Connected Users: ${clients.size})")
        }
    }
}