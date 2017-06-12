package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.chat.model.action.Action
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.config.model.ActionConfig
import java.util.Queue
import java.util.regex.Pattern

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
    val tokens: Queue<String>
    val templateRegex: Pattern?
    val actionMessageMode: ActionMessageMode
    val placeholderValues: MutableMap<String, String>

    /**
     * Convert the action templates to a human-readable String.
     */
    val actionTemplates: String

    /**
     * Process the token and return true if it was accepted.
     */
    fun accept(input: String): Boolean

    /**
     * Hook for subclasses.
     */
    fun onTemplateSatisfied() = true

    /**
     * List the actions from this template.
     */
    fun buildActions(): List<Action>

    /**
     * Build the message for when this action starts.
     */
    fun buildStartMessage(options: Map<String, String> = emptyMap(),
                          actionConfig: ActionConfig? = null): String

    /**
     * Build the message for when this action completes.
     */
    fun buildCompleteMessage(): String = ""
}

enum class ActionMessageMode {
    INDIVIDUAL,
    GROUP;
}
