package com.gatehill.rundeckbot.chat

import com.gatehill.rundeckbot.chat.model.Action
import com.gatehill.rundeckbot.chat.model.CustomAction
import com.gatehill.rundeckbot.chat.model.SystemAction
import com.gatehill.rundeckbot.config.ConfigService
import com.gatehill.rundeckbot.config.model.ActionConfig
import com.gatehill.rundeckbot.config.model.TransformType
import com.gatehill.rundeckbot.config.model.readActionConfigAttribute
import java.util.*

private val configService by lazy { ConfigService }
private val templateService by lazy { TemplateService }

/**
 * Actions that can be performed. Doubles as a list of named permissions in the security configuration.
 */
enum class ActionType(val description: String) {
    TRIGGER("trigger job"),
    ENABLE("enable job"),
    DISABLE("disable job"),
    LOCK("lock"),
    UNLOCK("unlock"),
    STATUS("check status"),
    HELP("show help")
}

/**
 * An abstract representation of a templated action.
 */
interface ActionTemplate {
    val builtIn: Boolean
    val showInUsage: Boolean
    val actionType: ActionType
    val tokens: Queue<String>

    /**
     * Convert the action templates to a human-readable String.
     */
    val actionTemplates: String

    /**
     * Process the token and return true if it was accepted.
     */
    fun accept(input: String): Boolean

    /**
     * List the actions from this template.
     */
    fun buildActions(): List<Action>
}

/**
 * Parses tokens into placeholder values.
 */
abstract class AbstractActionTemplate : ActionTemplate {
    protected val placeholderValues: MutableMap<String, String> = HashMap()
    protected abstract val actionConfigs: List<ActionConfig>

    override val actionTemplates: String
        get() = readActionConfigAttribute(actionConfigs, ActionConfig::template)

    override fun accept(input: String): Boolean {
        if (tokens.size == 0) return false
        val token = tokens.poll()

        val match = "\\{(.*)\\}".toRegex().matchEntire(token)
        if (null == match) {
            // syntactic sugar
            return (token == input)

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

/**
 * Represents a system action.
 */
abstract class SystemActionTemplate : AbstractActionTemplate() {
    override val builtIn = true
    override val actionConfigs: List<ActionConfig> = emptyList()

    override fun buildActions(): List<Action> {
        return listOf(SystemAction(actionType, buildShortDescription(), buildMessage()))
    }
}

/**
 * Represents a custom action.
 */
abstract class CustomActionTemplate : AbstractActionTemplate() {
    /**
     * List the actions from this template.
     */
    override fun buildActions(): List<Action> {
        return actionConfigs.map { actionConfig ->
            val options = transform(actionConfig, placeholderValues)
            CustomAction(actionType,
                    buildShortDescription(actionConfig),
                    buildMessage(options, actionConfig),
                    actionConfig, options)
        }
    }

    private fun transform(actionConfig: ActionConfig, options: MutableMap<String, String>): Map<String, String> {
        val transformed: MutableMap<String, String> = HashMap(options)

        actionConfig.transformers?.forEach { optionTransform ->
            val optionKey = optionTransform.key

            var optionValue = options[optionKey]
            if (null != optionValue) {
                optionTransform.value.forEach { transformType ->
                    optionValue = when (transformType) {
                        TransformType.LOWERCASE -> optionValue!!.toLowerCase()
                        TransformType.UPPERCASE -> optionValue!!.toUpperCase()
                        else -> throw UnsupportedOperationException("Transform type ${transformType} is not supported")
                    }
                }
                transformed[optionKey] = optionValue!!
            }
        }

        return transformed
    }
}

/**
 * Represents a simple operation for a named action.
 */
abstract class NamedActionTemplate : CustomActionTemplate() {
    protected val actionPlaceholder = "action name"
    override val builtIn: Boolean = true
    override val showInUsage: Boolean = true
    override val actionConfigs: MutableList<ActionConfig> = mutableListOf()

    override fun accept(input: String): Boolean {
        val accepted = super.accept(input)

        // has action been set?
        if (accepted && tokens.isEmpty()) {
            val actionName = placeholderValues[actionPlaceholder]
            val potentialConfigs = configService.actions()

            val actionConfig = potentialConfigs[actionName]
            if (null != actionConfig) {
                // exact action name match
                this.actionConfigs.add(actionConfig)

            } else {
                // check tags
                potentialConfigs.values.forEach { potentialConfig ->
                    potentialConfig.tags
                            ?.filter { tag -> tag == actionName || actionName == "all" }
                            ?.forEach { tag -> this.actionConfigs.add(potentialConfig) }
                }

                return (this.actionConfigs.size > 0)
            }
        }

        return accepted
    }
}

/**
 * Shows a help/usage message.
 */
class ShowHelpTemplate : SystemActionTemplate() {
    override val showInUsage = false
    override val actionType = ActionType.HELP
    override val tokens = LinkedList(listOf("help"))

    override fun buildMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        return "${ChatLines.greeting()} :simple_smile: Try one of these:\r\n${templateService.usage()}"
    }
}

/**
 * Triggers job execution.
 */
class TriggerJobTemplate : CustomActionTemplate {
    override val builtIn: Boolean = false
    override val showInUsage: Boolean = true
    override val actionType: ActionType = ActionType.TRIGGER
    override val actionConfigs: List<ActionConfig>
    override val tokens: Queue<String>

    constructor(action: ActionConfig) {
        this.actionConfigs = mutableListOf(action)
        tokens = LinkedList(action.template.split("\\s".toRegex()).filterNot(String::isBlank))
    }

    override fun buildMessage(options: Map<String, String>, actionConfig: ActionConfig?): String {
        actionConfig ?: throw IllegalArgumentException("Empty actionConfig")

        val msg = StringBuilder()
        msg.append("${ChatLines.pleaseWait()}, I'm running *${actionConfig.name}*")

        if (options.size > 0) {
            msg.append(" with these options:")
            options.forEach { arg -> msg.append("\r\n- ${arg.key}: _${arg.value}_") }
        } else {
            msg.append(".")
        }

        return msg.toString()
    }
}

class EnableJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.ENABLE
    override val tokens = LinkedList(listOf("enable", "{${actionPlaceholder}}"))
}

class DisableJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.DISABLE
    override val tokens = LinkedList(listOf("disable", "{${actionPlaceholder}}"))
}

class LockActionTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.LOCK
    override val tokens = LinkedList(listOf("lock", "{${actionPlaceholder}}"))
}

class UnlockActionTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.UNLOCK
    override val tokens = LinkedList(listOf("unlock", "{${actionPlaceholder}}"))
}

class StatusJobTemplate : NamedActionTemplate() {
    override val actionType: ActionType = ActionType.STATUS
    override val tokens = LinkedList(listOf("status", "{${actionPlaceholder}}"))
}
