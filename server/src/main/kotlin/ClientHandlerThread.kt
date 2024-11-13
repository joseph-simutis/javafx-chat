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
                        if (session.account?.first != null) {
                            if (input == "Logout") {
                                val username = session.account!!.first
                                session.account = null
                                session.writeLine("LLogged out successfully!")
                                command.echo("Session $uuid has logged out of account $username")
                                command.broadcast("$username has disconnected.")
                            }
                            else session.writeLine("EYou are already logged in!")
                        } else {
                            val credentials = input.drop(1).split(';')
                            if (credentials.size != 2) {
                                session.writeLine("EInvalid Credentials Format!")
                            } else if (!command.accounts.contains(credentials[0])) {
                                session.writeLine("EInvalid username or password.")
                            } else if (!BCrypt.verifyer()
                                    .verify(
                                        credentials[1].toCharArray(),
                                        command.accounts[credentials[0]]!!.passwordHash
                                    ).verified
                            ) {
                                session.writeLine("EInvalid username or password.")
                            } else {
                                var alreadyLoggedIn = false
                                for ((_, client) in command.clients) {
                                    if (client.account?.first == credentials[0]) {
                                        alreadyLoggedIn = true
                                    }
                                }
                                if (alreadyLoggedIn) session.writeLine("EThis account is already in use at another location!")
                                else {
                                    session.writeLine("LLogin Successful!")
                                    session.account = Pair(credentials[0], command.accounts[credentials[0]]!!)
                                    command.echo("Session $uuid has logged into account ${session.account?.first}.")
                                    command.broadcast("${session.account?.first} has connected.")
                                }
                            }
                        }
                    }

                    'R' -> {
                        if (session.account?.first != null) {
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
                                    AccountInfo(
                                        BCrypt.withDefaults().hashToString(10, credentials[1].toCharArray()),
                                        false,
                                        false
                                    )
                                )
                                session.writeLine("RRegister Successful! Please log into your new account.")
                                command.configFile.writeText(Json.encodeToString(command.accounts))
                                command.echo("Session $uuid has registered an account by the name of ${credentials[0]}.")
                            }
                        }
                    }

                    'M' -> {
                        if (session.account?.first == null) {
                            session.writeLine("EMust be logged in to send a message!")
                        } else {
                            val message = input.drop(1)
                            if (message.first() == '/') {
                                val command2 = message.drop(1).split(' ')
                                when (command2[0]) {
                                    "help" -> session.writeLine("MCommands:\n/help: Prints this message.\n/account: Lists the information about your account.${if (session.account?.second?.admin == true) "\n/ban: Bans an account." else ""}")
                                    "account" -> session.writeLine("MYour username is ${session.account?.first}.${if (session.account?.second?.admin == true) " You are also an admin." else ""}")
                                    "ban" -> {
                                        if (session.account?.second?.admin == true) {
                                            if (command2.size != 2) session.writeLine("EIncorrect arguments!")
                                            else if (!command.accounts.contains(command2[1])) session.writeLine("ENo such user!")
                                            else if (command.accounts[command2[1]]?.banned == true) session.writeLine("E${command2[1]} is already banned!")
                                            else {
                                                command.clients.forEach { (_, session2) ->
                                                    if (session2.account?.first == command2[1] && command.accounts[command2[1]]?.banned != true) {
                                                        command.accounts[command2[1]]?.banned = true
                                                        session2.writeLine("Banned!")
                                                        command.clients[uuid]?.account = null
                                                        command.broadcast("M${session.account?.first} has been banned.")
                                                    }
                                                }
                                                session.writeLine("MBanned ${command2[1]}!")
                                            }
                                        } else session.writeLine("EIncorrect permissions! Use /help for help!")
                                    }
                                    "unban" -> if (session.account?.second?.admin == true) {
                                        if (command2.size != 2) session.writeLine("EIncorrect arguments!")
                                        else if (!command.accounts.contains(command2[1])) session.writeLine("ENo such user!")
                                        else if (command.accounts[command2[1]]?.banned == false) session.writeLine("E${command2[1]} is already unbanned!")
                                        else {
                                            command.accounts[command2[1]]?.banned = false
                                            session.writeLine("MUnbanned ${command2[1]}!")
                                        }
                                    } else session.writeLine("EIncorrect permissions! Use /help for help!")

                                    else -> session.writeLine("MInvalid command! Use /help for help.")
                                }
                            } else {
                                command.broadcast("${session.account?.first}: $message")
                                command.echo("Session $uuid User ${session.account?.first} -> \"$message\"")
                            }
                        }
                    }

                    'E' -> {
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