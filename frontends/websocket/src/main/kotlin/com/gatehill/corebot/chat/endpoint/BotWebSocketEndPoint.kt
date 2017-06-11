package com.gatehill.corebot.chat.endpoint

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.MessageService
import com.gatehill.corebot.chat.SessionHolder
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.config.ChatSettings
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
            sessionService.connectedSessions.add(SessionHolder(session))
        }

        "User ${session.id} connected".let {
            logger.info(it)
            if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll(it)
        }
    }

    @OnMessage
    fun getMessage(message: String, session: Session) {
        if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll("User ${session.id} says > $message")

        sessionService.findTriggerSession(session.id).let { (_, username, realName) ->
            val trigger = TriggerContext(session.id, username, realName, Instant.now().toEpochMilli().toString(), null)
            messageService.handleMessage(trigger, message)
        }
    }

    @OnClose
    fun closeConnectionHandler(session: Session, closeReason: CloseReason) {
        sessionService.connectedSessions.removeAll { it.session == session }

        "User ${session.id} disconnected".let {
            logger.info(it)
            if (ChatSettings.echoEventsToAllSessions) sessionService.broadcastToAll(it)
        }
    }
}
