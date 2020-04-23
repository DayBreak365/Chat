package me.breakofday.chat.client

import com.mommoo.flat.layout.linear.LinearLayout
import com.mommoo.flat.layout.linear.Orientation
import com.mommoo.flat.layout.linear.constraints.LinearConstraints
import com.mommoo.flat.layout.linear.constraints.LinearSpace
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.TextArea
import java.awt.TextField
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField


class ClientGUI : JFrame(), Observer {

    init {
        Observer.attachObserver(this)
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(500, 700)
        title = "Chat"
        layout = LinearLayout(Orientation.VERTICAL, 0)
        isVisible = true
    }

    private val contentPanel = JPanel().also {
        it.layout = LinearLayout(Orientation.VERTICAL)
        this.add(it, LinearConstraints().setWeight(10).setLinearSpace(LinearSpace.MATCH_PARENT))
    }

    private val console = TextArea().also {
        it.isEditable = false
        contentPanel.add(it, LinearConstraints().setWeight(12).setLinearSpace(LinearSpace.MATCH_PARENT))
    }

    private val messageInput = TextField().also {
        contentPanel.add(it, LinearConstraints().setWeight(1).setLinearSpace(LinearSpace.MATCH_PARENT))
        it.isEnabled = false
        it.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    sendMessage(it.text)
                    it.text = ""
                }
            }
        })
    }

    private val inputPanel = JPanel().also {
        it.layout = FlowLayout()
        this.add(it, LinearConstraints().setWeight(2).setLinearSpace(LinearSpace.MATCH_PARENT))
    }
    val ipField = JTextField().also {
        val panel = JPanel()
        panel.add(JLabel("Server IP"))
        it.preferredSize = Dimension(100, 20)
        panel.add(it)
        inputPanel.add(panel)
    }
    val portField = JTextField().also {
        val panel = JPanel()
        panel.add(JLabel("Port"))
        it.preferredSize = Dimension(100, 20)
        panel.add(it)
        inputPanel.add(panel)
    }
    val nicknameField = JTextField().also {
        val panel = JPanel()
        panel.add(JLabel("Nickname"))
        it.preferredSize = Dimension(100, 20)
        panel.add(it)
        inputPanel.add(panel)
    }

    private val buttonPanel = JPanel().also {
        inputPanel.add(it)
    }
    private val connectButton = JButton().also { jButton ->
        jButton.preferredSize = Dimension(100, 20)
        jButton.text = "Connect"
        jButton.addActionListener(object : ActionListener {
            override fun actionPerformed(e: ActionEvent?) {
                val port: Int
                try {
                    port = Integer.parseInt(portField.text).also {
                        if (it < 0 || it > 0xFFFF) {
                            throw IllegalArgumentException()
                        }
                    }
                } catch (e: Exception) {
                    writeLine("올바르지 않은 포트입니다.")
                    return
                }
                connect(ipField.text, port, nicknameField.text)
            }
        })
        buttonPanel.add(jButton)
    }
    private val disconnectButton = JButton().also {
        it.preferredSize = Dimension(100, 20)
        it.text = "Disconnect"
        it.isEnabled = false
        it.addActionListener { disconnect() }
        buttonPanel.add(it)
    }

    override fun connecting() {
        connectButton.isEnabled = false
        disconnectButton.isEnabled = false
        messageInput.isEnabled = false
        messageInput.text = ""
    }

    override fun onConnect() {
        connectButton.isEnabled = false
        disconnectButton.isEnabled = true
        messageInput.isEnabled = true
        messageInput.text = ""
    }

    override fun onDisconnect() {
        connectButton.isEnabled = true
        disconnectButton.isEnabled = false
        messageInput.isEnabled = false
        messageInput.text = ""
    }

    fun writeLine(line: String) {
        console.append(line + System.lineSeparator())
    }

}