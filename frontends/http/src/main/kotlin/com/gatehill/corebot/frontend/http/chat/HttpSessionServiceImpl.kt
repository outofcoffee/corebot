package com.gatehill.corebot.frontend.http.chat

import com.gatehill.corebot.frontend.session.chat.StatefulSessionServiceImpl
import com.gatehill.corebot.operation.model.TriggerContext
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Manages HTTP sessions.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class HttpSessionServiceImpl : StatefulSessionServiceImpl<RoutingContext, HttpSessionHolder>(), HttpSessionService {
    private val logger: Logger = LogManager.getLogger(HttpSessionServiceImpl::class.java)

    override fun sendMessage(trigger: TriggerContext, message: String) {
        if (message.isNotBlank()) {
            try {
                findTriggerSession(trigger).session.response().end(message)
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }

    override fun terminateSessionInternal(trigger: TriggerContext) {
//        findTriggerSession(trigger).session.close(CloseReason(
//                CloseReason.CloseCodes.GOING_AWAY,
//                "${trigger.username} terminated the session"
//        ))
        // TODO
    }
}
