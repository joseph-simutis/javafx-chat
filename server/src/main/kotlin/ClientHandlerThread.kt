package io.github.josephsimutis.server

import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.josephsimutis.common.packet.PacketHeader
import java.util.*

class ClientHandlerThread(private val command: ChatServerCommand, private val uuid: UUID) : Thread() {
    override fun run() {
        val session = command.clients[uuid]!!
        try {
            while (true) {
                val input = session.connection.readPacket()
                when (input.header) {
                    PacketHeader.LOGIN -> {
                        if (session.account?.first != null) session.connection.writePacket(PacketHeader.ERROR, "You are already logged in!")
                        else {
                            if (input.data.size != 2) {
                                session.connection.writePacket(PacketHeader.ERROR, "Invalid Credentials Format!")
                            } else if (!command.accounts.contains(input.data[0])) {
                                session.connection.writePacket(PacketHeader.ERROR, "Invalid username or password.")
                            } else if (!BCrypt.verifyer()
                                    .verify(
                                        input.data[1].toCharArray(),
                                        command.accounts[input.data[0]]!!.passwordHash
                                    ).verified
                            ) {
                                session.connection.writePacket(PacketHeader.ERROR, "Invalid username or password.")
                            } else if (command.accounts[input.data[0]]!!.banned) {
                                session.connection.writePacket(PacketHeader.ERROR, "This account is banned.")
                            } else {
                                var alreadyLoggedIn = false
                                for ((_, client) in command.clients) {
                                    if (client.account?.first == input.data[0]) {
                                        alreadyLoggedIn = true
                                    }
                                }
                                if (alreadyLoggedIn) session.connection.writePacket(PacketHeader.ERROR, "This account is already in use at another location!")
                                else {
                                    session.connection.writePacket(PacketHeader.LOGIN)
                                    session.account = Pair(input.data[0], command.accounts[input.data[0]]!!)
                                    command.echo("Session $uuid has logged into account ${session.account?.first}.")
                                    command.broadcast("${session.account?.first} has connected.")
                                }
                            }
                        }
                    }

                    PacketHeader.LOGOUT -> {
                        if (session.account?.first != null) {
                            val username = session.account!!.first
                            session.account = null
                            session.connection.writePacket(PacketHeader.LOGOUT)
                            command.echo("Session $uuid has logged out of account $username.")
                            command.broadcast("$username has disconnected.")
                        }
                    }

                    PacketHeader.REGISTER -> {
                        if (session.account?.first != null) {
                            session.connection.writePacket(PacketHeader.ERROR, "Already logged in!")
                        } else {
                            if (input.data.size != 2) {
                                session.connection.writePacket(PacketHeader.ERROR, "Invalid Credentials Format!")
                            } else if (command.accounts.contains(input.data[0])) {
                                session.connection.writePacket(PacketHeader.ERROR, "Username is taken!")
                            } else {
                                command.accounts += Pair(
                                    input.data[0],
                                    AccountInfo(
                                        BCrypt.withDefaults().hashToString(10, input.data[1].toCharArray()),
                                        admin=false,
                                        banned=false
                                    )
                                )
                                session.connection.writePacket(PacketHeader.REGISTER, "Register Successful! Please log into your new account.")
                                command.updateAccountFile()
                                command.echo("Session $uuid has registered an account by the name of ${input.data[0]}.")
                            }
                        }
                    }

                    PacketHeader.MESSAGE -> {
                        if (session.account?.first == null) {
                            session.connection.writePacket(PacketHeader.ERROR, "Must be logged in to send a message!")
                        } else {
                            val message = input.data[0]
                            if (message[0] == '/') {
                                val command2 = message.drop(1).split(' ')
                                when (command2[0]) {
                                    "help" -> session.connection.writePacket(PacketHeader.MESSAGE, "Commands:\n/help: Prints this message.\n/account: Lists the information about your account.${if (session.account?.second?.admin == true) "\n/ban: Bans an account." else ""}")
                                    "account" -> session.connection.writePacket(PacketHeader.MESSAGE, "Your username is ${session.account?.first}.${if (session.account?.second?.admin == true) " You are also an admin." else ""}")
                                    "ban" -> {
                                        if (session.account?.second?.admin == true) {
                                            if (command2.size != 2) session.connection.writePacket(PacketHeader.ERROR, "Incorrect arguments!")
                                            else if (!command.accounts.contains(command2[1])) session.connection.writePacket(PacketHeader.ERROR, "No such user!")
                                            else if (command.accounts[command2[1]]?.banned == true) session.connection.writePacket(PacketHeader.ERROR, "${command2[1]} is already banned!")
                                            else {
                                                command.clients.forEach { (_, session2) ->
                                                    if (session2.account?.first == command2[1] && command.accounts[command2[1]]?.banned != true) {
                                                        command.accounts[command2[1]]?.banned = true
                                                        session2.connection.writePacket(PacketHeader.BAN)
                                                        command.clients[uuid]?.account = null
                                                        command.updateAccountFile()
                                                        command.broadcast("${session.account?.first} has been banned.")
                                                    }
                                                }
                                                session.connection.writePacket(PacketHeader.MESSAGE, "Banned ${command2[1]}!")
                                            }
                                        } else session.connection.writePacket(PacketHeader.ERROR, "Incorrect permissions! Use /help for help.")
                                    }
                                    "unban" -> if (session.account?.second?.admin == true) {
                                        if (command2.size != 2) session.connection.writePacket(PacketHeader.ERROR, "Incorrect arguments!")
                                        else if (!command.accounts.contains(command2[1])) session.connection.writePacket(PacketHeader.ERROR, "No such user!")
                                        else if (command.accounts[command2[1]]?.banned == false) session.connection.writePacket(PacketHeader.ERROR, "${command2[1]} is already unbanned!")
                                        else {
                                            command.accounts[command2[1]]?.banned = false
                                            command.updateAccountFile()
                                            session.connection.writePacket(PacketHeader.MESSAGE, "Unbanned ${command2[1]}!")
                                        }
                                    } else session.connection.writePacket(PacketHeader.ERROR, "Incorrect permissions! Use /help for help.")

                                    else -> session.connection.writePacket(PacketHeader.ERROR, "Invalid command! Use /help for help.")
                                }
                            } else {
                                command.broadcast("${session.account?.first}: $message")
                                command.echo("Session $uuid User ${session.account?.first} -> \"$message\"")
                            }
                        }
                    }

                    PacketHeader.ERROR -> {
                        command.echo("Session $uuid User ${session.account?.first} -> Error: ${input.data}")
                    }

                    else -> {
                        session.connection.writePacket(PacketHeader.ERROR, "Invalid packet header!")
                    }
                }
            }
        } catch (e: Exception) {
            command.echo("Session $uuid has disconnected!")
            command.broadcast("${session.account?.first} has disconnected.")
            session.connection.socket.close()
            command.clients.remove(uuid)
        }
    }
}