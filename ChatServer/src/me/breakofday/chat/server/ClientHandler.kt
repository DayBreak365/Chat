package me.breakofday.chat.server

import me.breakofday.chat.packet.Packet
import me.breakofday.chat.packet.PacketClientData
import me.breakofday.chat.packet.PacketClientDisconnect
import me.breakofday.chat.packet.PacketMessage
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class ClientHandler(private val client: Socket) : Thread() {

    companion object {
        val handlers = HashSet<ClientHandler>()

        fun broadcastPacket(packet: Packet) {
            for (handler in handlers) {
                try {
                    handler.outputStream.writeObject(packet)
                } catch (e: Exception) {
                }
            }
        }
    }

    private lateinit var inputStream: ObjectInputStream
    lateinit var outputStream: ObjectOutputStream
    private lateinit var nickname: String

    init {
        try {
            client.soTimeout = 3000
            inputStream = ObjectInputStream(client.getInputStream())
            outputStream = ObjectOutputStream(client.getOutputStream())
            inputStream.readObject().also {
                if (it is PacketClientData) {
                    this.nickname = it.nickname
                } else {
                    throw Exception()
                }
            }
            client.soTimeout = 0
            handlers.add(this)
            start()
        } catch (e: Exception) {
            handlers.remove(this)
            outputStream.writeObject(PacketClientDisconnect(e.message ?: ""))
            client.close()
            println("$nickname disconnected.")
            broadcastPacket(PacketMessage("$nickname 님이 떠나셨습니다."))
        }
    }

    override fun run() {
        println("$nickname connected.")
        broadcastPacket(PacketMessage("$nickname 님이 접속하셨습니다."))
        try {
            while (true) {
                when (val packet = inputStream.readObject()) {
                    is PacketClientDisconnect -> {
                        println("$nickname disconnected" + if (packet.reason.isEmpty()) "." else (": " + packet.reason))
                        broadcastPacket(PacketMessage("$nickname 님이 떠나셨습니다."))
                    }
                    is PacketMessage -> {
                        println("<$nickname>: " + packet.message)
                        broadcastPacket(PacketMessage("<$nickname>: " + packet.message))
                    }
                }
            }
        } catch (e: Exception) {
        } finally {
            handlers.remove(this)
            println("$nickname disconnected.")
            broadcastPacket(PacketMessage("$nickname 님이 떠나셨습니다."))
            client.close()
        }
    }

}
