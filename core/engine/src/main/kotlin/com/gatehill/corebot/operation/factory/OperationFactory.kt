package com.gatehill.corebot.operation.factory

import com.gatehill.corebot.operation.model.Operation
import com.gatehill.corebot.operation.model.OperationType
import com.gatehill.corebot.operation.model.TriggerContext
import com.gatehill.corebot.chat.filter.FilterConfig
import com.gatehill.corebot.config.model.ActionConfig

/**
 * Produces operations of a given type. Must be annotated with `com.gatehill.corebot.action.factory.Template`
 * to provide metadata.
 */
interface OperationFactory {
    val parsers: MutableList<FilterConfig>
    val operationType: OperationType
    val operationMessageMode: OperationMessageMode
    val placeholderValues: MutableMap<String, String>

    /**
     * Reads the template metadata for this factory.
     */
    fun readMetadata(): Template = readOperationFactoryMetadata(this::class.java)

    /**
     * Hook for subclasses to do things like manipulate placeholders once
     * the template has been fully satisfied.
     */
    fun onSatisfied() = true

    /**
     * List the operations from this template.
     */
    fun buildOperations(trigger: TriggerContext): List<Operation>

    /**
     * Lifecycle hook for subclasses, invoked after `onSatisfied` and `buildOperations`, but before performing the operation.
     */
    fun beforePerform(trigger: TriggerContext) {
        /* no op */
    }

    /**
     * Build the message for when this operation starts.
     */
    fun buildStartMessage(trigger: TriggerContext,
                          options: Map<String, String> = emptyMap(),
                          actionConfig: ActionConfig? = null): String

    /**
     * Build the message for when this operation completes.
     */
    fun buildCompleteMessage(): String = ""
}

/**
 * Reads the template metadata for a factory.
 */
fun readOperationFactoryMetadata(factoryClass: Class<out OperationFactory>): Template =
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
                          val operationMessageMode: OperationMessageMode,
                          val placeholderKeys: Array<String> = emptyArray())

enum class OperationMessageMode {
    /**
     * Each operation emits a message.
     */
    INDIVIDUAL,

    /**
     * Emit a message at the start and end of the group of operations.
     */
    GROUP;
}
