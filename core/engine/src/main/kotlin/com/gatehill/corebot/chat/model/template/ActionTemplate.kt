package com.gatehill.corebot.chat.model.template

import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.model.action.Action
import com.gatehill.corebot.chat.model.action.ActionType
import com.gatehill.corebot.chat.parser.ParserConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val parsers: MutableList<ParserConfig>
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
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

/**
 * Must be implemented by templates that can have regex parser config.
 */
interface PlaceholderKeysTemplate {
    val placeholderKeys: List<String>
}

interface RegexActionTemplate : ActionTemplate, PlaceholderKeysTemplate

enum class ActionMessageMode {
    INDIVIDUAL,
    GROUP;
}
