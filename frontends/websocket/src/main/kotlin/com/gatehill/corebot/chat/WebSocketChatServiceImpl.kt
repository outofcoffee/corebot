package com.gatehill.corebot.chat

import com.gatehill.corebot.chat.endpoint.BotWebSocketEndPoint
import com.gatehill.corebot.config.ChatSettings.hostname
import com.gatehill.corebot.config.ChatSettings.port
import org.glassfish.tyrus.server.Server

/**
 * Handles WebSocket conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class WebSocketChatServiceImpl : ChatService {
    private val server: Server by lazy { Server(hostname, port, "/", setOf(BotWebSocketEndPoint::class.java)) }

    override fun listenForEvents() {
        server.start()
    }

    override fun stopListening() {
        server.stop()
    }
}
