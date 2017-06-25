package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.parser.StringParser
import com.gatehill.corebot.config.model.ActionConfig

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

    init {
        this.actionConfigs = mutableListOf(action)
        parsers += StringParser.StringParserConfig(action.template, action.template)
    }

    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?): String {
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
