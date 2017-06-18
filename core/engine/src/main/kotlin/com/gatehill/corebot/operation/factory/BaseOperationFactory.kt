package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Parses tokens into placeholder values.
 */
abstract class BaseOperationFactory : OperationFactory {
    final override val operationMessageMode: OperationMessageMode
        get() = readMetadata().operationMessageMode

    override val parsers = mutableListOf<FilterConfig>()
    override val placeholderValues = mutableMapOf<String, String>()

    /**
     * A short, human readable description.
     */
    protected fun buildShortDescription(actionConfig: ActionConfig? = null) =
            actionConfig?.let { "_${operationType.description}_ on *${actionConfig.name}*" } ?: run { "_${operationType.description}_" }

    /**
     * The response message sent when this operationType is fired.
     */
    override fun buildStartMessage(trigger: TriggerContext, options: Map<String, String>, actionConfig: ActionConfig?) =
            actionConfig?.let { "I'm working on *${actionConfig.name}*..." } ?: run { "I'm working on it..." }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseOperationFactory) return false
        return (javaClass != other.javaClass)
    }

    override fun hashCode(): Int = javaClass.hashCode()

    override fun toString(): String = this::class.java.canonicalName
}
