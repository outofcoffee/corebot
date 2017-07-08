package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Parses tokens into placeholder values.
 */
abstract class BaseActionFactory : ActionFactory {
    final override val actionMessageMode: ActionMessageMode
        get() = readMetadata().actionMessageMode

    override val parsers = mutableListOf<FilterConfig>()
    override val placeholderValues = mutableMapOf<String, String>()
    protected abstract val actionConfigs: List<ActionConfig>

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
        if (other !is BaseActionFactory) return false
        return (javaClass != other.javaClass)
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
