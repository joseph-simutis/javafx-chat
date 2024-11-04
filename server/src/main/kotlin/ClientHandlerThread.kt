package io.github.josephsimutis.server

import at.favre.lib.crypto.bcrypt.BCrypt
import java.net.SocketTimeoutException

class ClientHandlerThread(val command: ChatServerCommand, val index: Int) : Thread() {
    override fun run() {
        val session = command.clients[index]
        try {
            while (true) {
                val input = session.readLine()
                when (input.first()) {
                    'L' -> {
                        if (session.username != null) {
                            session.writeLine("EAlready logged in!")
                        } else {
                            val credentials = input.drop(1).split(';')
                            if (credentials.size != 2) {
                                session.writeLine("EInvalid Credentials Format!")
                            } else if (!command.accounts.contains(credentials[0])) {
                                session.writeLine("EInvalid username or password.")
                            } else if (!BCrypt.verifyer()
                                    .verify(credentials[1].toCharArray(), command.accounts[credentials[0]]).verified
                            ) {
                                session.writeLine("EInvalid username or password.")
                            } else {
                                session.username = credentials[0]
                                session.writeLine("LLogin Successful!")
                                command.echo("Session $index has logged into account ${session.username}.")
                            }
                        }
                    }

                    'R' -> {
                        if (session.username != null) {
                            session.writeLine("EAlready logged in!")
                        } else {
                            val credentials = input.drop(1).split(';')
                            if (credentials.size != 2) {
                                session.writeLine("EInvalid Credentials Format!")
                            } else if (command.accounts.contains(credentials[0])) {
                                session.writeLine("EUsername is taken!")
                            } else {
                                command.accounts += Pair(
                                    credentials[0],
                                    BCrypt.withDefaults().hashToString(10, credentials[1].toCharArray())
                                )
                                session.writeLine("RRegister Successful! Please log into your new account.")
                                command.echo("Session $index has registered an account by the name of ${session.username}.")
                            }
                        }
                    }

                    'M' -> {
                        if (session.username == null) {
                            session.writeLine("EMust be logged in to send a message!")
                        } else {
                            val message = input.drop(1)
                            for (index2 in 0..command.clients.lastIndex) {
                                if (index != index2) {
                                    command.clients[index2].writeLine("M$message")
                                }
                            }
                            command.echo("Session $index, User ${session.username} -> \"$message\"")
                        }
                    }

                    'E' -> {
                        command.echo("Session $index, User ${session.username} -> Error: ${input.drop(1)}")
                    }

                    'H' -> {
                        //session.writeLine("Heartbeat")
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            command.echo("Session $index has disconnected!")
            session.socket.close()
            command.clients.removeAt(index)
        }
    }
}