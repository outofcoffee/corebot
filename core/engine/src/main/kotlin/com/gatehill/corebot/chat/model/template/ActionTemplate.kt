package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
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
     * Describe the action templates as a human-readable `String`.
     */
    val actionTemplates: String

    /**
     * Hook for subclasses to do things like manipulate placeholders once
     * the template has been fully satisfied.
     */
    fun onTemplateSatisfied() = true

    /**
     * List the actions from this template.
     */
    fun buildActions(trigger: TriggerContext): List<Action>

    /**
     * Build the message for when this action starts.
     */
    fun buildStartMessage(trigger: TriggerContext,
                          options: Map<String, String> = emptyMap(),
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
