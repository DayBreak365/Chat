package me.breakofday.chat.client

import me.breakofday.chat.packet.PacketClientData
import me.breakofday.chat.packet.PacketClientDisconnect
import me.breakofday.chat.packet.PacketMessage
import me.breakofday.chat.packet.PacketServerClosed
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ConnectException
import java.net.Socket
import java.net.UnknownHostException
import java.util.LinkedList

val clientGUI = ClientGUI()

fun main(args: Array<String>) {}

private var socket: Socket? = null
private var outputStream: ObjectOutputStream? = null
private var inputStream: ObjectInputStream? = null
private var packetReader: Thread? = null

fun connect(ip: String, port: Int, nickname: String) {
    if (isDisconnected()) {
        Observer.connecting()
        object : Thread() {
            override fun run() {
                try {
                    socket = Socket(ip, port)
                    Observer.onConnect()
                    outputStream = ObjectOutputStream(socket?.getOutputStream())
                    inputStream = ObjectInputStream(socket?.getInputStream())
                    packetReader = object : Thread() {
                        override fun run() {
                            try {
                                packetReader@ while (true) {
                                    when (val packet = inputStream?.readObject()) {
                                        is PacketServerClosed -> {
                                            clientGUI.writeLine("서버가 닫혔습니다.")
                                            break@packetReader
                                        }
                                        is PacketClientDisconnect -> {
                                            clientGUI.writeLine("연결이 끊어졌습니다" + if (packet.reason.isEmpty()) "." else (": " + packet.reason))
                                            break@packetReader
                                        }
                                        is PacketMessage -> {
                                            clientGUI.writeLine(packet.message)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                            } finally {
                                disconnect()
                                Observer.onDisconnect()
                            }
                        }
                    }
                    outputStream?.writeObject(PacketClientData(nickname))
                    outputStream?.flush()
                    packetReader?.start()
                } catch (e: Exception) {
                    when (e) {
                        is ConnectException -> {
                            clientGUI.writeLine("연결에 실패했습니다.")
                        }
                        is UnknownHostException -> {
                            clientGUI.writeLine("연결에 실패했습니다. (알 수 없는 호스트: $ip)")
                        }
                    }
                    Observer.onDisconnect()
                }
            }
        }.start()
    }
}

fun sendMessage(message: String) {
    if (isConnected()) {
        outputStream?.writeObject(PacketMessage(message))
        outputStream?.flush()
    }
}

fun isConnected(): Boolean {
    return socket != null
}

fun disconnect() {
    if (isConnected()) {
        try {
            outputStream?.writeObject(PacketClientDisconnect())
            outputStream?.flush()
        } catch (e: Exception) {
        }
        packetReader?.interrupt()
        socket?.close()
        socket = null
        outputStream = null
        inputStream = null
        packetReader = null
        Observer.onDisconnect()
        clientGUI.writeLine("연결이 끊어졌습니다.")
    }
}

fun isDisconnected(): Boolean {
    return socket == null
}

interface Observer {

    companion object {
        private val observers = LinkedList<Observer>()
        fun attachObserver(observer: Observer) {
            observers.add(observer)
        }

        fun connecting() {
            for (observer in observers) observer.connecting()
        }

        fun onConnect() {
            for (observer in observers) observer.onConnect()
        }

        fun onDisconnect() {
            for (observer in observers) observer.onDisconnect()
        }
    }

    fun connecting()
    fun onConnect()
    fun onDisconnect()
}
