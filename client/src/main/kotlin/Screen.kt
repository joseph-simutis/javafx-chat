package io.github.josephsimutis.client

import javafx.animation.FadeTransition
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.util.Duration
import java.io.IOException
import java.net.Socket

enum class Screen {
    PRECONNECT {
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
                                    app.socket = Socket((hBox.children[1] as TextField).text, (hBox.children[3]as TextField).text.toInt())
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
    LOGIN {
        override fun draw(app: ChatClientApplication) = VBox(Text("login screen goes here"))
    };

    abstract fun draw(app: ChatClientApplication): Parent
}