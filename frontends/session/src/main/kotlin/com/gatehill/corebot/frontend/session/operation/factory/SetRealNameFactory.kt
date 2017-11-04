package com.gatehill.corebot.frontend.session.operation.factory

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.frontend.session.chat.StatefulSessionService
import com.gatehill.corebot.frontend.session.operation.model.SessionOperationType
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.PlainOperationFactory
import com.gatehill.corebot.operation.factory.Template
import javax.inject.Inject

/**
 * Sets the real name for the current session.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Template("setRealName", showInUsage = true, builtIn = true, operationMessageMode = OperationMessageMode.GROUP,
        placeholderKeys = arrayOf(SetRealNameFactory.realNamePlaceholder)
)
class SetRealNameFactory @Inject constructor(private val sessionService: StatefulSessionService<*, *>) : PlainOperationFactory() {
    override val operationType = SessionOperationType.SET_REAL_NAME

    private val realName: String
        get() = placeholderValues[realNamePlaceholder]!!

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) = ""

    override fun beforePerform(trigger: TriggerContext) {
        sessionService.findTriggerSession(trigger).realName = realName
    }

    override fun buildCompleteMessage() = "Real name set to $realName"

    companion object {
        const val realNamePlaceholder = "realName"
    }
}
