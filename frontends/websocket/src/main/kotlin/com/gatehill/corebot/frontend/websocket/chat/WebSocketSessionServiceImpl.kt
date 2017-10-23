package com.gatehill.corebot.frontend.websocket.chat

import com.gatehill.corebot.frontend.session.chat.StatefulSessionServiceImpl
import com.gatehill.corebot.frontend.websocket.config.ChatSettings
import com.gatehill.corebot.operation.model.TriggerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import javax.websocket.CloseReason

/**
 * Manages WebSocket sessions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class WebSocketSessionServiceImpl : StatefulSessionServiceImpl(), WebSocketSessionService {
    private val logger: Logger = LogManager.getLogger(StatefulSessionServiceImpl::class.java)

    override fun broadcastToAll(message: String) {
        connectedSessions.forEach {
            try {
                it.wsSession.session.basicRemote.sendText(message)
            } catch (e: IOException) {
                logger.error(e)
            }
        }
    }

    override fun sendMessage(trigger: TriggerContext, message: String) {
        if (message.isNotBlank()) {
            if (ChatSettings.echoEventsToAllSessions) {
                broadcastToAll(message)

            } else {
                // FIXME DRY
                try {
                    findTriggerSession(trigger).wsSession.session.basicRemote.sendText(message)
                } catch (e: IOException) {
                    logger.error(e)
                }
            }
        }
    }

    override fun terminateSessionInternal(trigger: TriggerContext) {
        findTriggerSession(trigger).wsSession.session.close(CloseReason(
                CloseReason.CloseCodes.GOING_AWAY,
                "${trigger.username} terminated the session"
        ))
    }
}
