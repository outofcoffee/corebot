package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.parser.ParserConfig
import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.readActionConfigAttribute

/**
 * Parses tokens into placeholder values.
 */
abstract class BaseActionTemplate : ActionTemplate {
    override val parsers = mutableListOf<ParserConfig>()
    protected abstract val actionConfigs: List<ActionConfig>
    override val placeholderValues = mutableMapOf<String, String>()

    override val actionTemplates: String
        get() = readActionConfigAttribute(actionConfigs, ActionConfig::template)

    /**
     * A short, human readable description.
     */
    protected fun buildShortDescription(actionConfig: ActionConfig? = null) =
            actionConfig?.let { "_${actionType.description}_ on *${actionConfig.name}*" } ?: run { "_${actionType.description}_" }

    /**
     * The response message sent when this actionType is fired.
     */
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            actionConfig?.let { "I'm working on *${actionConfig.name}*..." } ?: run { "I'm working on it..." }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseActionTemplate) return false
        return (javaClass != other.javaClass)
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
