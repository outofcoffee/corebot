package com.gatehill.corebot.action.factory

import com.gatehill.corebot.action.model.Action
import com.gatehill.corebot.action.model.ActionType
import com.gatehill.corebot.action.model.TriggerContext
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Produces actions of a given type. Must be annotated with `com.gatehill.corebot.action.factory.Template`
 * to provide metadata.
 */
interface ActionFactory {
    val parsers: MutableList<FilterConfig>
    val actionType: ActionType
    val actionMessageMode: ActionMessageMode
    val placeholderValues: MutableMap<String, String>

    /**
     * Reads the template metadata for this factory.
     */
    fun readMetadata(): Template = readActionFactoryMetadata(this::class.java)

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
 * Reads the template metadata for a factory.
 */
fun readActionFactoryMetadata(factoryClass: Class<out ActionFactory>): Template =
        factoryClass.getAnnotationsByType(Template::class.java).firstOrNull()
                ?: throw IllegalStateException("Missing @Template annotation for: ${factoryClass.canonicalName}")

/**
 * Metadata for a template.
 * Specifying `placeholderKeys` allows regex templates.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Template(val templateName: String,
                          val builtIn: Boolean,
                          val showInUsage: Boolean,
                          val actionMessageMode: ActionMessageMode,
                          val placeholderKeys: Array<String> = emptyArray())

enum class ActionMessageMode {
    INDIVIDUAL,
    GROUP;
}
