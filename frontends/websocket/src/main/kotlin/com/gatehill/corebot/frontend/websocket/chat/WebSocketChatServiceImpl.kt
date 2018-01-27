package com.gatehill.corebot.frontend.websocket.chat

import com.gatehill.corebot.chat.ChatService
import com.gatehill.corebot.frontend.websocket.chat.endpoint.BotWebSocketEndPoint
import com.gatehill.corebot.frontend.websocket.config.ChatSettings.hostname
import com.gatehill.corebot.frontend.websocket.config.ChatSettings.port
import org.glassfish.tyrus.server.Server

/**
 * Handles WebSocket conversations.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
open class WebSocketChatServiceImpl : ChatService {
    override val supportsUserTermination = true

    private val server: Server by lazy { Server(hostname, port, "/", setOf(BotWebSocketEndPoint::class.java)) }

    override fun listenForEvents() {
        server.start()
    }

    override fun stopListening() {
        server.stop()
    }
}
