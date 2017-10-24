package com.gatehill.corebot.frontend.http.chat

import com.gatehill.corebot.frontend.session.chat.StatefulSessionServiceImpl
import com.gatehill.corebot.operation.model.TriggerContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.UnsupportedOperationException

/**
 * Manages HTTP sessions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpSessionServiceImpl : StatefulSessionServiceImpl(), HttpSessionService {
    private val logger: Logger = LogManager.getLogger(HttpSessionServiceImpl::class.java)

    override fun sendMessage(trigger: TriggerContext, message: String) {
        if (message.isNotBlank()) {
            try {
                findTriggerSession(trigger).httpSession.session.response().end(message)
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    override fun terminateSessionInternal(trigger: TriggerContext) {
        throw UnsupportedOperationException()
    }
}
