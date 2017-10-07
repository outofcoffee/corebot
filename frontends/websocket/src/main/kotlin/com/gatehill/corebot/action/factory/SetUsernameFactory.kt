package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.action.model.WebSocketOperationType
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Sets the username for the current session.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Template("setUsername", showInUsage = true, builtIn = true, operationMessageMode = OperationMessageMode.GROUP,
        placeholderKeys = arrayOf(SetUsernameFactory.usernamePlaceholder)
)
class SetUsernameFactory @Inject constructor(private val sessionService: WebSocketSessionService) : PlainOperationFactory() {
    override val operationType = WebSocketOperationType.SET_USERNAME

    private val username: String
        get() = placeholderValues[usernamePlaceholder]!!

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    override fun beforePerform(trigger: TriggerContext) {
        sessionService.findTriggerSession(trigger).username = username
    }

    override fun buildCompleteMessage() = "Username set to $username"

    companion object {
        const val usernamePlaceholder = "username"
    }
}
