package io.github.josephsimutis.server

import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class ClientHandlerThread(private val command: ChatServerCommand, private val uuid: UUID) : Thread() {
    override fun run() {
        val session = command.clients[uuid]!!
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
                                    .verify(credentials[1].toCharArray(), command.accounts[credentials[0]]!!.passwordHash).verified
                            ) {
                                session.writeLine("EInvalid username or password.")
                            } else {
                                session.writeLine("LLogin Successful!")
                                session.username = credentials[0]
                                session.account = command.accounts[credentials[0]]
                                command.echo("Session $uuid has logged into account ${session.username}.")
                                command.broadcast("${session.username} has connected.")
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
                                    AccountInfo(BCrypt.withDefaults().hashToString(10, credentials[1].toCharArray()), false, false)
                                )
                                session.writeLine("RRegister Successful! Please log into your new account.")
                                command.configFile.writeText(Json.encodeToString(command.accounts))
                                command.echo("Session $uuid has registered an account by the name of ${credentials[0]}.")
                            }
                        }
                    }

                    'M' -> {
                        if (session.username == null) {
                            session.writeLine("EMust be logged in to send a message!")
                        } else {
                            val message = input.drop(1)
                            if (message.first() == '/') {
                                val command2 = message.drop(1).split(' ')
                                when (command2[0]) {
                                    "help" -> {}
                                    "account" -> session.writeLine("MYour username is ${session.username}.${if (session.account?.admin == true) " You are also an admin." else ""}")
                                    "ban" -> {
                                        if (session.account?.admin == true) {
                                            if (command2.size != 2) session.writeLine("MIncorrect arguments!")
                                            else if (!command.accounts.contains(command2[1])) session.writeLine("MNo such user!")
                                            else if (command.accounts[command2[1]]?.banned == true) session.writeLine("${command2[1]} is already banned!")
                                            else {
                                                command.accounts[command2[1]]?.banned = true
                                                session.writeLine("MBanned ${command2[1]}!")
                                            }
                                        } else session.writeLine("MIncorrect permissions! Use /help for help!")
                                    }
                                    else -> session.writeLine("MInvalid command! Use /help for help.")
                                }
                            } else {
                                command.broadcast("${session.username}: $message")
                                command.echo("Session $uuid User ${session.username} -> \"$message\"")
                            }
                        }
                    }

                    'E' -> {
                        command.echo("Session $uuid User ${session.username} -> Error: ${input.drop(1)}")
                    }
                }
            }
        } catch (e: Exception) {
            command.echo("Session $uuid has disconnected!")
            command.broadcast("${session.username} has disconnected.")
            session.socket.close()
            command.clients.remove(uuid)
        }
    }
}