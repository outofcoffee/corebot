package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.LinkedList
import java.util.Queue

/**
 * Triggers job execution.
 */
class TriggerJobTemplate(action: ActionConfig,
                         private val chatGenerator: ChatGenerator) : CustomActionTemplate() {

    override val builtIn = false
    override val showInUsage = true
    override val actionType: ActionType = JobActionType.TRIGGER
    override val actionMessageMode = ActionMessageMode.INDIVIDUAL
    override val actionConfigs: List<ActionConfig>
    override val tokens: Queue<String>

    init {
        this.actionConfigs = mutableListOf(action)
        tokens = LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
    }

    override fun buildStartMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        actionConfig ?: throw IllegalArgumentException("Empty actionConfig")

        val msg = StringBuilder()
        msg.append("${chatGenerator.pleaseWait()}, I'm running *${actionConfig.name}*")

        if (options.isNotEmpty()) {
            msg.append(" with these options:")
            options.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}
