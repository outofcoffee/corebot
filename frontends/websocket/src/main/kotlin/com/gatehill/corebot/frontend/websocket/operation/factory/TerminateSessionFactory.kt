package com.gatehill.corebot.frontend.websocket.operation.factory

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.frontend.websocket.operation.model.WebSocketOperationType
import com.gatehill.corebot.frontend.websocket.chat.WebSocketSessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.PlainOperationFactory
import com.gatehill.corebot.operation.factory.Template
import javax.inject.Inject

/**
 * Terminates the current session.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Template("terminateSession", showInUsage = true, builtIn = true, operationMessageMode = OperationMessageMode.GROUP)
class TerminateSessionFactory @Inject constructor(private val sessionService: WebSocketSessionService) : PlainOperationFactory() {
    override val operationType = WebSocketOperationType.TERMINATE_SESSION

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = "Bye!"

    override fun beforePerform(trigger: TriggerContext) {
        sessionService.terminateSession(trigger)
    }
}
