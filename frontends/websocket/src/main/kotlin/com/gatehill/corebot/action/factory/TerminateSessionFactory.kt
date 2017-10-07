package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.action.model.WebSocketOperationType
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.config.model.ActionConfig
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
