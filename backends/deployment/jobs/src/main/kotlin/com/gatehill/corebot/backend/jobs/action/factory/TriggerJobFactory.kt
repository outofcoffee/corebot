package com.gatehill.corebot.backend.jobs.action.factory

import com.gatehill.corebot.operation.factory.OperationMessageMode
import com.gatehill.corebot.operation.factory.ActionOperationFactory
import com.gatehill.corebot.operation.factory.Template
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.ChatGenerator
import com.gatehill.corebot.chat.filter.StringFilter
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Triggers job execution.
 */
@Template("triggerJob", builtIn = false, showInUsage = true, operationMessageMode = OperationMessageMode.INDIVIDUAL)
class TriggerJobFactory(action: ActionConfig,
                        private val chatGenerator: ChatGenerator) : ActionOperationFactory() {

    override val operationType: OperationType = JobOperationType.TRIGGER
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
