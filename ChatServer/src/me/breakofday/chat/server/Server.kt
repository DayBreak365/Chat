package me.breakofday.chat.server

import me.breakofday.chat.packet.PacketServerClosed
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import java.net.ServerSocket
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            for (handler in ClientHandler.handlers) {
                handler.outputStream.writeObject(PacketServerClosed())
            }
            println("서버가 닫혔습니다.")
        }
    })
    val options = Options()
    options.addOption(Option("port", true, "서버 포트").apply {
        isRequired = true
    })
    val cmd: CommandLine
    try {
        cmd = DefaultParser().parse(options, args)
    } catch (e: ParseException) {
        println(e.message)
        HelpFormatter().printHelp("server -port <port>", "\n\n", options, "")
        exitProcess(0)
    }
    val port: Int
    try {
        port = Integer.parseInt(cmd.getOptionValue("port")).also {
            if (it < 0 || it > 0xFFFF) {
                throw IllegalArgumentException()
            }
        }
    } catch (e: NumberFormatException) {
        println("올바르지 않은 포트입니다.")
        exitProcess(0)
    } catch (e: IllegalArgumentException) {
        println("올바르지 않은 포트입니다.")
        exitProcess(0)
    }

    println("Starting chat server on port $port...")
    Server(port)
}

class Server(port: Int) {

    private val server: ServerSocket = ServerSocket(port)

    init {
        println("Chat server started.")
        while (true) {
            server.accept().also {
                println("Accepted from: " + it.inetAddress)
                ClientHandler(it)
            }
        }

    }

}
