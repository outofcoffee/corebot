package com.gatehill.corebot.frontend.websocket.chat

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.frontend.websocket.config.ChatSettings
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import javax.websocket.CloseReason

/**
 * Manages WebSocket sessions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class WebSocketSessionServiceImpl : WebSocketSessionService {
    private val logger: Logger = LogManager.getLogger(WebSocketSessionServiceImpl::class.java)

    override val connectedSessions = mutableListOf<SessionHolder>()

    override val botUsername: String
        get() {
            logger.warn("botUsername is not implemented")
            return ""
        }

    override fun broadcastToAll(message: String) {
        connectedSessions.forEach { (session) ->
            try {
                session.basicRemote.sendText(message)
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
                    findTriggerSession(trigger).session.basicRemote.sendText(message)
                } catch (e: IOException) {
                    logger.error(e)
                }
            }
        }
    }

    override fun addReaction(trigger: TriggerContext, emojiCode: String) {
        logger.warn("addReaction is not implemented")
    }

    override fun findTriggerSession(trigger: TriggerContext) = findTriggerSession(trigger.channelId)

    override fun findTriggerSession(sessionId: String) =
            connectedSessions.first { it.session.id == sessionId }

    override fun lookupUsername(trigger: TriggerContext, userId: String): String =
            findTriggerSession(trigger).username

    override fun lookupUserRealName(trigger: TriggerContext, userId: String): String =
            findTriggerSession(trigger).realName

    override fun terminateSession(trigger: TriggerContext) {
        logger.info("${trigger.username} terminated the session")

        findTriggerSession(trigger).session.close(CloseReason(
                CloseReason.CloseCodes.GOING_AWAY,
                "${trigger.username} terminated the session"
        ))
    }
}
