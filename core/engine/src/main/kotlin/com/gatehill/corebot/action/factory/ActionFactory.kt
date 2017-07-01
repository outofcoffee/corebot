package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.Action
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Produces actions of a given type.
 */
interface ActionFactory {
    val parsers: MutableList<FilterConfig>
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
    val actionMessageMode: ActionMessageMode
    val placeholderValues: MutableMap<String, String>

    /**
     * Hook for subclasses to do things like manipulate placeholders once
     * the template has been fully satisfied.
     */
    fun onSatisfied() = true

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
 * Metadata for a template.
 * Specifying `placeholderKeys` allows regex templates.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Template(val templateName: String,
                          val placeholderKeys: Array<String> = emptyArray())

enum class ActionMessageMode {
    INDIVIDUAL,
    GROUP;
}
