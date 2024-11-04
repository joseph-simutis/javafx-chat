package io.github.josephsimutis.client

class HeartbeatThread(val app: ChatClientApplication) : Thread() {
    var keepRunning = true

    override fun run() {
        while (app.socket?.isConnected == true && app.socket?.isClosed == false && keepRunning) {
            app.writeLine("Heartbeat")
            sleep(1000)
        }
    }
}