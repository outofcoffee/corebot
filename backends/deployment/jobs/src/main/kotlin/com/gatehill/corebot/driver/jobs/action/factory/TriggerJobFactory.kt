package com.gatehill.corebot.driver.jobs.action.factory

import com.gatehill.corebot.action.factory.ActionMessageMode
import com.gatehill.corebot.action.factory.CustomActionFactory
import com.gatehill.corebot.action.factory.Template
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.filter.StringFilter
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Triggers job execution.
 */
@Template("triggerJob", builtIn = false, showInUsage = true, actionMessageMode = ActionMessageMode.INDIVIDUAL)
class TriggerJobFactory(action: ActionConfig,
                        private val chatGenerator: ChatGenerator) : CustomActionFactory() {

    override val actionType: ActionType = JobActionType.TRIGGER
    override val actionConfigs: List<ActionConfig>

    init {
        this.actionConfigs = mutableListOf(action)
        parsers += StringFilter.StringFilterConfig(action.template, action.template)
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
