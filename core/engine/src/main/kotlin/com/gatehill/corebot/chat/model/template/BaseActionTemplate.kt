package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.readActionConfigAttribute

/**
 * Parses tokens into placeholder values.
 */
abstract class BaseActionTemplate : ActionTemplate {
    protected val placeholderValues = mutableMapOf<String, String>()
    protected abstract val actionConfigs: List<ActionConfig>

    override val actionTemplates: String
        get() = readActionConfigAttribute(actionConfigs, ActionConfig::template)

    override fun accept(input: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val accepted: Boolean

        val match = "\\{(.*)}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            accepted = token.equals(input, ignoreCase = true)

        } else {
            // option placeholder
            placeholderValues[match.groupValues[1]] = input
            accepted = true
        }

        return if (accepted && tokens.isEmpty()) onTemplateSatisfied() else accepted
    }

    /**
     * Hook for subclasses.
     */
    open fun onTemplateSatisfied() = true

    /**
     * A short, human readable description.
     */
    protected fun buildShortDescription(actionConfig: ActionConfig? = null) =
            actionConfig?.let { "_${actionType.description}_ on *${actionConfig.name}*" } ?: run { "_${actionType.description}_" }

    /**
     * The response message sent when this actionType is fired.
     */
    override fun buildStartMessage(options: Map<String, String>, actionConfig: ActionConfig?) =
            actionConfig?.let { "I'm working on *${actionConfig.name}*..." } ?: run { "I'm working on it..." }
}
