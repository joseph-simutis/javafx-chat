package io.github.josephsimutis.client

import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
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
                                    app.socket = Socket(
                                        (hBox.children[1] as TextField).text,
                                        (hBox.children[3] as TextField).text.toInt()
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
            FadeTransition(Duration.seconds(5.0), messageBox).apply {
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
                        app.writeLine("L${((vBox.children[1] as HBox).children[1] as TextField).text};${((vBox.children[2] as HBox).children[1] as PasswordField).text}")
                        val input = app.readLine()
                        when (input.first()) {
                            'L' -> {
                                app.changeScreen(CHAT)
                            }

                            'E' -> {
                                ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                                ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                                printToScreen(input.drop(1))
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
            FadeTransition(Duration.seconds(5.0), messageBox).apply {
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
                        app.writeLine("R${((vBox.children[1] as HBox).children[1] as TextField).text};${((vBox.children[2] as HBox).children[1] as PasswordField).text}")
                        val input = app.readLine()
                        ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                        ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                        printToScreen(input.drop(1))
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
            FadeTransition(Duration.seconds(5.0), messageBox).apply {
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
                        app.writeLine("M$text")
                        text = ""
                    }
                },
                Button("Logout").apply {
                    onAction = EventHandler {
                        app.changeScreen(LOGIN)
                        app.writeLine("Logout")
                    }
                }
            )
            vBox.minWidthProperty().bind(app.primaryStage.scene.widthProperty())
            vBox.minHeightProperty().bind(app.primaryStage.scene.heightProperty())
        }

        override fun printToScreen(str: String) {
            messageBox.text += "\n$str"
        }
    };

    abstract fun draw(app: ChatClientApplication): Parent
    abstract fun printToScreen(str: String)
}