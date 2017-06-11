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
class SetUsernameTemplate @Inject constructor(private val sessionService: WebSocketSessionService) : SystemActionTemplate() {
    override val showInUsage = true
    override val actionType = WebSocketActionType.SET_USERNAME
    override val tokens = LinkedList(listOf("username", "{$usernamePlaceholder}"))
    override val actionMessageMode: ActionMessageMode = ActionMessageMode.INDIVIDUAL

    private val username: String
        get() = placeholderValues[usernamePlaceholder]!!

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
        sessionService.findTriggerSession(trigger).username = username
        return "Username set to $username"
    }

    companion object {
        val usernamePlaceholder = "username"
    }
}
