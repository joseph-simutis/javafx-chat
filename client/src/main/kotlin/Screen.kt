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
                                var exception = false
                                try {
                                    app.socket = Socket(
                                        (hBox.children[1] as TextField).text,
                                        (hBox.children[3] as TextField).text.toInt()
                                    )
                                } catch (e: IOException) {
                                    (vBox.children[2] as Text).text = "Cannot connect to server: ${e.message}"
                                    exception = true
                                } catch (e: NumberFormatException) {
                                    (vBox.children[2] as Text).text = "Cannot convert port to a number: ${e.message}"
                                    exception = true
                                }
                                if (exception) FadeTransition(Duration.seconds(5.0), vBox.children[2]).apply {
                                    fromValue = 1.0
                                    toValue = 0.0
                                    play()
                                } else {
                                    app.changeScreen(LOGIN)
                                }
                            }
                        }
                    )
                    hBox.alignment = Pos.CENTER
                },
                Text("")
            )
            vBox.alignment = Pos.CENTER
        }
    },
    LOGIN(600.0, 400.0) {
        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            vBox.children.addAll(
                Text("Login").apply { font = Font.font(36.0) },
                HBox(Text("Username:"), TextField()),
                HBox(Text("Password:"), PasswordField()),
                Button("Login").apply {
                    onAction = EventHandler {
                        app.writeLine("L${((vBox.children[1] as HBox).children[1] as TextField).text};${((vBox.children[2] as HBox).children[1] as PasswordField).text}")
                        val input = app.readLine()!!
                        when (input.first()) {
                            'L' -> {
                                app.changeScreen(CHAT)
                            }

                            'E' -> {
                                (vBox.children[5] as Text).text = input.drop(1)
                                ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                                ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                                FadeTransition(Duration.seconds(5.0), vBox.children[5]).apply {
                                    fromValue = 1.0
                                    toValue = 0.0
                                    play()
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
                Text("")
            )
            vBox.alignment = Pos.CENTER
        }
    },
    REGISTER(600.0, 400.0) {
        override fun draw(app: ChatClientApplication) = VBox().also { vBox ->
            vBox.children.addAll(
                Text("Register").apply { font = Font.font(36.0) },
                HBox(Text("Username:"), TextField()),
                HBox(Text("Password:"), PasswordField()),
                Button("Register").apply {
                    onAction = EventHandler {
                        app.writeLine("R${((vBox.children[1] as HBox).children[1] as TextField).text};${((vBox.children[2] as HBox).children[1] as PasswordField).text}")
                        val input = app.readLine()!!
                        (vBox.children[5] as Text).text = input.drop(1)
                        ((vBox.children[1] as HBox).children[1] as TextField).text = ""
                        ((vBox.children[2] as HBox).children[1] as PasswordField).text = ""
                        FadeTransition(Duration.seconds(5.0), vBox.children[5]).apply {
                            fromValue = 1.0
                            toValue = 0.0
                            play()
                        }
                    }
                },
                Button("Switch to login screen").apply {
                    onAction = EventHandler {
                        app.changeScreen(LOGIN)
                    }
                },
                Text("")
            )
            vBox.alignment = Pos.CENTER
        }
    },
    CHAT(600.0, 400.0) {
        override fun draw(app: ChatClientApplication) = HBox().also { hBox ->
            ServerHandlerThread(app).start()
            hBox.children.addAll(
                TextField(),
                Button("Send").apply {
                    onAction = EventHandler {
                        app.writeLine("M${(hBox.children[0] as TextField).text}")
                        (hBox.children[0] as TextField).text = ""
                    }
                }
            )
            hBox.minWidthProperty().bind(app.primaryStage.scene.widthProperty())
            hBox.minHeightProperty().bind(app.primaryStage.scene.heightProperty())
        }
    };

    abstract fun draw(app: ChatClientApplication): Parent
}