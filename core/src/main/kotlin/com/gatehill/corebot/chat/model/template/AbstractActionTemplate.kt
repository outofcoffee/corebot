package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.config.model.ActionConfig
import com.gatehill.corebot.config.model.readActionConfigAttribute

/**
 * Parses tokens into placeholder values.
 */
abstract class AbstractActionTemplate : ActionTemplate {
    protected val placeholderValues = mutableMapOf<String, String>()
    protected abstract val actionConfigs: List<ActionConfig>

    override val actionTemplates: String
        get() = readActionConfigAttribute(actionConfigs, ActionConfig::template)

    override fun accept(input: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val match = "\\{(.*)\\}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            return token.equals(input, ignoreCase = true)

        } else {
            // option placeholder
            placeholderValues[match.groupValues[1]] = input
            return true
        }
    }

    /**
     * A short, human readable description.
     */
    protected fun buildShortDescription(actionConfig: ActionConfig? = null): String {
        return if (null != actionConfig) "_${actionType.description}_ on *${actionConfig.name}*" else "_${actionType.description}_"
    }

    /**
     * The response message sent when this actionType is fired.
     */
    protected open fun buildMessage(options: Map<String, String> = emptyMap(),
                                    actionConfig: ActionConfig? = null): String {

        return if (null != actionConfig) "I'm working on *${actionConfig.name}*..." else "I'm working on it..."
    }
}
