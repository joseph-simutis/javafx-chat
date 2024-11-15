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
                val input = session.readPacket()
                when (input.first()) {
                    "Login" -> {
                        if (session.account?.first != null) session.writePacket(listOf("Error", "You are already logged in!"))
                        else {
                            val credentials = input.drop(1)
                            if (credentials.size != 2) {
                                session.writePacket(listOf("Error", "Invalid Credentials Format!"))
                            } else if (!command.accounts.contains(credentials[0])) {
                                session.writePacket(listOf("Error", "Invalid username or password."))
                            } else if (!BCrypt.verifyer()
                                    .verify(
                                        credentials[1].toCharArray(),
                                        command.accounts[credentials[0]]!!.passwordHash
                                    ).verified
                            ) {
                                session.writePacket(listOf("Error", "Invalid username or password."))
                            } else if (command.accounts[credentials[0]]!!.banned) {
                                session.writePacket(listOf("Error", "This account is banned."))
                            } else {
                                var alreadyLoggedIn = false
                                for ((_, client) in command.clients) {
                                    if (client.account?.first == credentials[0]) {
                                        alreadyLoggedIn = true
                                    }
                                }
                                if (alreadyLoggedIn) session.writePacket(listOf("Error", "This account is already in use at another location!"))
                                else {
                                    session.writePacket(listOf("Login"))
                                    session.account = Pair(credentials[0], command.accounts[credentials[0]]!!)
                                    command.echo("Session $uuid has logged into account ${session.account?.first}.")
                                    command.broadcast("${session.account?.first} has connected.")
                                }
                            }
                        }
                    }

                    "Logout" -> {
                        if (session.account?.first != null) {
                            val username = session.account!!.first
                            session.account = null
                            session.writePacket(listOf("Logout"))
                            command.echo("Session $uuid has logged out of account $username")
                            command.broadcast("$username has disconnected.")
                        }
                    }

                    "Register" -> {
                        if (session.account?.first != null) {
                            session.writePacket(listOf("Error", "Already logged in!"))
                        } else {
                            val credentials = input.drop(1)
                            if (credentials.size != 2) {
                                session.writePacket(listOf("Error", "Invalid Credentials Format!"))
                            } else if (command.accounts.contains(credentials[0])) {
                                session.writePacket(listOf("Error", "Username is taken!"))
                            } else {
                                command.accounts += Pair(
                                    credentials[0],
                                    AccountInfo(
                                        BCrypt.withDefaults().hashToString(10, credentials[1].toCharArray()),
                                        admin=false,
                                        banned=false
                                    )
                                )
                                session.writePacket(listOf("Register", "Register Successful! Please log into your new account."))
                                command.updateConfigFile()
                                command.echo("Session $uuid has registered an account by the name of ${credentials[0]}.")
                            }
                        }
                    }

                    "Message" -> {
                        if (session.account?.first == null) {
                            session.writePacket(listOf("Error", "Must be logged in to send a message!"))
                        } else {
                            val message = input[1]
                            if (message.first() == '/') {
                                val command2 = message.drop(1).split(' ')
                                when (command2[0]) {
                                    "help" -> session.writePacket(listOf("Message", "Commands:\n/help: Prints this message.\n/account: Lists the information about your account.${if (session.account?.second?.admin == true) "\n/ban: Bans an account." else ""}"))
                                    "account" -> session.writePacket(listOf("Message", "Your username is ${session.account?.first}.${if (session.account?.second?.admin == true) " You are also an admin." else ""}"))
                                    "ban" -> {
                                        if (session.account?.second?.admin == true) {
                                            if (command2.size != 2) session.writePacket(listOf("Error", "Incorrect arguments!"))
                                            else if (!command.accounts.contains(command2[1])) session.writePacket(listOf("Error", "No such user!"))
                                            else if (command.accounts[command2[1]]?.banned == true) session.writePacket(listOf("Error", "${command2[1]} is already banned!"))
                                            else {
                                                command.clients.forEach { (_, session2) ->
                                                    if (session2.account?.first == command2[1] && command.accounts[command2[1]]?.banned != true) {
                                                        command.accounts[command2[1]]?.banned = true
                                                        session2.writePacket(listOf("Ban"))
                                                        command.clients[uuid]?.account = null
                                                        command.updateConfigFile()
                                                        command.broadcast("${session.account?.first} has been banned.")
                                                    }
                                                }
                                                session.writePacket(listOf("Message", "Banned ${command2[1]}!"))
                                            }
                                        } else session.writePacket(listOf("Error", "Incorrect permissions! Use /help for help."))
                                    }
                                    "unban" -> if (session.account?.second?.admin == true) {
                                        if (command2.size != 2) session.writePacket(listOf("Error", "Incorrect arguments!"))
                                        else if (!command.accounts.contains(command2[1])) session.writePacket(listOf("Error", "No such user!"))
                                        else if (command.accounts[command2[1]]?.banned == false) session.writePacket(listOf("Error", "${command2[1]} is already unbanned!"))
                                        else {
                                            command.accounts[command2[1]]?.banned = false
                                            session.writePacket(listOf("Message", "Unbanned ${command2[1]}!"))
                                        }
                                    } else session.writePacket(listOf("Error", "Incorrect permissions! Use /help for help."))

                                    else -> session.writePacket(listOf("Error", "Invalid command! Use /help for help."))
                                }
                            } else {
                                command.broadcast("${session.account?.first}: $message")
                                command.echo("Session $uuid User ${session.account?.first} -> \"$message\"")
                            }
                        }
                    }

                    "Error" -> {
                        command.echo("Session $uuid User ${session.account?.first} -> Error: ${input.drop(1)}")
                    }
                }
            }
        } catch (e: Exception) {
            command.echo("Session $uuid has disconnected!")
            command.broadcast("${session.account?.first} has disconnected.")
            session.socket.close()
            command.clients.remove(uuid)
        }
    }
}