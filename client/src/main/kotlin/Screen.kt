package io.github.josephsimutis.client

import io.github.josephsimutis.common.Connection
import io.github.josephsimutis.common.packet.PacketHeader
import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
import java.io.IOException
import java.net.Socket

enum class Screen(val width: Double, val height: Double) {
    PRECONNECT(600.0, 400.0) {
        private val messageBox = Text("")

        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            vBox.children.addAll(
                Text("Connect to Server").apply { font = Font.font(36.0) },
                HBox().also { hBox ->
                    hBox.children.addAll(
                        Text("IP:"),
                        TextField(),
                        Text("Port:"),
                        TextField(),
                        Button("Connect").also { button ->
                            button.onAction = EventHandler {
                                try {
                                    app.connection = Connection(
                                        Socket(
                                            (hBox.children[1] as TextField).text,
                                            (hBox.children[3] as TextField).text.toInt()
                                        )
                                    )
                                    app.changeScreen(LOGIN)
                                } catch (e: IOException) {
                                    printToScreen("Cannot connect to server: ${e.message}")
                                } catch (e: NumberFormatException) {
                                    printToScreen("Cannot convert port to a number: ${e.message}")
                                }
                            }
                        }
                    )
                    hBox.alignment = Pos.CENTER
                },
                messageBox
            )
            vBox.alignment = Pos.CENTER
        }

        override fun printToScreen(str: String) {
            messageBox.text = str
            FadeTransition(Duration.seconds(10.0), messageBox).apply {
                fromValue = 1.0
                toValue = 0.0
                play()
            }
        }
    },
    LOGIN(600.0, 400.0) {
        private val messageBox = Text("")

        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            vBox.children.addAll(
                Text("Login").apply { font = Font.font(36.0) },
                HBox(Text("Username:"), TextField()),
                HBox(Text("Password:"), PasswordField()),
                Button("Login").apply {
                    onAction = EventHandler {
                        app.connection!!.writePacket(
                            PacketHeader.LOGIN,
                            ((vBox.children[1] as HBox).children[1] as TextField).text,
                            ((vBox.children[2] as HBox).children[1] as PasswordField).text
                        )
                        app.connection!!.readPacket().also { packet ->
                            when (packet.header) {
                                PacketHeader.LOGIN -> {
                                    app.changeScreen(CHAT)
                                }

                                PacketHeader.ERROR -> {
                                    ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                                    ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                                    printToScreen(packet.data[0])
                                }

                                else -> {
                                    printToScreen("Server responded with invalid packet: ${packet.toPrintableString()}!")
                                }
                            }
                        }
                    }
                },
                Button("Switch to register screen").apply {
                    onAction = EventHandler {
                        app.changeScreen(REGISTER)
                    }
                },
                messageBox
            )
            vBox.alignment = Pos.CENTER
        }

        override fun printToScreen(str: String) {
            messageBox.text = str
            FadeTransition(Duration.seconds(10.0), messageBox).apply {
                fromValue = 1.0
                toValue = 0.0
                play()
            }
        }
    },
    REGISTER(600.0, 400.0) {
        private val messageBox = Text("")

        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            vBox.children.addAll(
                Text("Register").apply { font = Font.font(36.0) },
                HBox(Text("Username:"), TextField()),
                HBox(Text("Password:"), PasswordField()),
                Button("Register").apply {
                    onAction = EventHandler {
                        app.connection!!.writePacket(
                            PacketHeader.REGISTER,
                            ((vBox.children[1] as HBox).children[1] as TextField).text,
                            ((vBox.children[2] as HBox).children[1] as PasswordField).text
                        )
                        val packet = app.connection!!.readPacket()
                        ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                        ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                        printToScreen(packet.data[0])
                    }
                },
                Button("Switch to login screen").apply {
                    onAction = EventHandler {
                        app.changeScreen(LOGIN)
                    }
                },
                messageBox
            )
            vBox.alignment = Pos.CENTER
        }

        override fun printToScreen(str: String) {
            messageBox.text = str
            FadeTransition(Duration.seconds(10.0), messageBox).apply {
                fromValue = 1.0
                toValue = 0.0
                play()
            }
        }
    },
    CHAT(600.0, 400.0) {
        private val messageBox = TextArea()

        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            ServerHandlerThread(app).start()
            vBox.children.addAll(
                messageBox.apply {
                    text = ""
                    isEditable = false
                    prefHeightProperty().bind(vBox.heightProperty())
                },
                TextField().apply {
                    onAction = EventHandler {
                        if (text != "") app.connection!!.writePacket(PacketHeader.MESSAGE, text)
                        text = ""
                    }
                },
                HBox().also { hBox ->
                    hBox.children.addAll(
                        Button("Logout").apply {
                            onAction = EventHandler {
                                app.changeScreen(LOGIN)
                                app.connection!!.writePacket(PacketHeader.LOGOUT)
                            }
                        }
                    )
                }
            )
            vBox.minWidthProperty().bind(app.primaryStage.scene.widthProperty())
            vBox.minHeightProperty().bind(app.primaryStage.scene.heightProperty())
        }

        override fun printToScreen(str: String) {
            messageBox.text += "\n$str"
            messageBox.scrollTop = Double.MAX_VALUE
        }
    };

    abstract fun draw(app: ChatClientApplication): Parent
    abstract fun printToScreen(str: String)
}