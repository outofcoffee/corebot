package com.gatehill.corebot.frontend.websocket.chat.endpoint

import com.gatehill.corebot.chat.MessageService
import com.gatehill.corebot.frontend.websocket.chat.WebSocketSessionHolder
import com.gatehill.corebot.frontend.websocket.chat.WebSocketSessionService
import com.gatehill.corebot.frontend.websocket.config.ChatSettings
import com.gatehill.corebot.operation.model.TriggerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Instant
import javax.inject.Inject
import javax.websocket.CloseReason
import javax.websocket.EndpointConfig
import javax.websocket.OnClose
import javax.websocket.OnMessage
import javax.websocket.OnOpen
import javax.websocket.Session
import javax.websocket.server.ServerEndpoint

/**
 * Handles WebSocket lifecycle for a single endpoint.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@ServerEndpoint(value = "/", configurator = CustomConfigurator::class)
class BotWebSocketEndPoint @Inject constructor(private val sessionService: WebSocketSessionService,
                                               private val messageService: MessageService) {

    private val logger: Logger = LogManager.getLogger(BotWebSocketEndPoint::class.java)

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        if (!sessionService.connectedSessions.any { it.session == session }) {
            sessionService.connectedSessions += WebSocketSessionHolder(session)
        }

        with("User ${session.id} connected") {
            logger.info(this)
            if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll(this)
        }
    }

    @OnMessage
    fun onMessage(message: String, session: Session) {
        if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll("User ${session.id} says > $message")

        sessionService.findTriggerSession(session.id).let {
            val trigger = TriggerContext(session.id, it.username, it.realName, Instant.now().toEpochMilli().toString(), null)
            messageService.handleMessage(trigger, message)
        }
    }

    @OnClose
    fun closeConnectionHandler(session: Session, closeReason: CloseReason) {
        sessionService.connectedSessions.removeAll { it.session == session }

        with("User ${session.id} disconnected") {
            logger.info(this)
            if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll(this)
        }
    }
}
