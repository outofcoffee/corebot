package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.action.model.WebSocketOperationType
import com.gatehill.corebot.config.model.ActionConfig
import javax.inject.Inject

/**
 * Sets the real name for the current session.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Template("setRealName", showInUsage = true, builtIn = true, operationMessageMode = OperationMessageMode.GROUP,
        placeholderKeys = arrayOf(SetRealNameFactory.realNamePlaceholder)
)
class SetRealNameFactory @Inject constructor(private val sessionService: WebSocketSessionService) : PlainOperationFactory() {
    override val operationType = WebSocketOperationType.SET_REAL_NAME

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
