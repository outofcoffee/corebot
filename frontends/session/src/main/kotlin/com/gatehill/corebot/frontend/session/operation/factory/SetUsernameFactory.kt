package com.gatehill.corebot.frontend.session.operation.factory

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.frontend.session.operation.model.SessionOperationType
import com.gatehill.corebot.frontend.session.chat.StatefulSessionService
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.PlainOperationFactory
import com.gatehill.corebot.operation.factory.Template
import javax.inject.Inject

/**
 * Sets the username for the current session.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Template("setUsername", showInUsage = true, builtIn = true, operationMessageMode = OperationMessageMode.GROUP,
        placeholderKeys = arrayOf(SetUsernameFactory.usernamePlaceholder)
)
class SetUsernameFactory @Inject constructor(private val sessionService: StatefulSessionService) : PlainOperationFactory() {
    override val operationType = SessionOperationType.SET_USERNAME

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
