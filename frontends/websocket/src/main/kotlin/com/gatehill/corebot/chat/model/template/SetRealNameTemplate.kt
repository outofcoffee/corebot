package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.WebSocketSessionService
import com.gatehill.corebot.chat.model.action.WebSocketActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.LinkedList
import javax.inject.Inject

/**
 *
 * @author pete
 */
class SetRealNameTemplate @Inject constructor(private val sessionService: WebSocketSessionService) : SystemActionTemplate() {
    override val showInUsage = true
    override val actionType = WebSocketActionType.SET_REAL_NAME
    override val tokens = LinkedList(listOf("real", "name", "{$realNamePlaceholder}"))
    override val actionMessageMode: ActionMessageMode = ActionMessageMode.INDIVIDUAL

    private val realName: String
        get() = placeholderValues[realNamePlaceholder]!!

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        sessionService.findTriggerSession(trigger).realName = realName
        return "Real name set to $realName"
    }

    companion object {
        val realNamePlaceholder = "real name"
    }
}
