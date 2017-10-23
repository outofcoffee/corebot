package com.gatehill.corebot.frontend.session.chat

import com.gatehill.corebot.operation.model.TriggerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Manages stateful sessions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class StatefulSessionServiceImpl<S, H : SessionHolder<S>> : StatefulSessionService<S, H> {
    private val logger: Logger = LogManager.getLogger(StatefulSessionServiceImpl::class.java)

    override val connectedSessions = mutableListOf<H>()

    override val botUsername: String
        get() {
            logger.warn("botUsername is not implemented")
            return ""
        }

    override fun addReaction(trigger: TriggerContext, emojiCode: String) {
        logger.warn("addReaction is not implemented")
    }

    override fun findTriggerSession(trigger: TriggerContext) = findTriggerSession(trigger.channelId)

    override fun findTriggerSession(sessionId: String) =
            connectedSessions.first { it.sessionId == sessionId }

    override fun lookupUsername(trigger: TriggerContext, userId: String): String =
            findTriggerSession(trigger).username

    override fun lookupUserRealName(trigger: TriggerContext, userId: String): String =
            findTriggerSession(trigger).realName

    override fun terminateSession(trigger: TriggerContext) {
        logger.info("${trigger.username} terminated the session")
        terminateSessionInternal(trigger)
    }

    abstract fun terminateSessionInternal(trigger: TriggerContext)
}
